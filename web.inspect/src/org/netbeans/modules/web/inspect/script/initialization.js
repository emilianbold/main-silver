/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

NetBeans = new Object();

// Name of attribute used for element's identification
NetBeans.ID_ATTRIBUTE = ' netbeansId';

// Name of the parameter (in query string)
// that is used to force reload of some resource
NetBeans.RELOAD_PARAM = 'netbeans_reload';

// Counter used to generate element IDs
NetBeans.nextId = 0;

// Array of selected elements - every item of this array is in the form
// {element: theActualElement; ... additional element data ... }
NetBeans.selection = [];

// Returns XML encoding of document elements.
// The XML contains name of the elements and
// NetBeans ID attributes only.
NetBeans.getDOM = function() {
    var self = this;
    var code = function(e,idx) {
        var result = '';
        if (e.nodeType == 1) {
            // Set ID attribute if necessary
            if (typeof(e[self.ID_ATTRIBUTE]) === 'undefined') {
                e[self.ID_ATTRIBUTE] = self.nextId++;
            }
                
            result += '<' + e.tagName;
            result += ' id=\"'+e[self.ID_ATTRIBUTE]+'\"';
            if (idx !== undefined) {
                result += ' idx=\"'+idx+'\"';
            }
            result += '>';
            var i;
            for (i=0; i<e.childNodes.length; i++) {
                result += code(e.childNodes[i],i);
            }
            result += '</' + e.tagName + '>';
        }
        return result;
    }
    return code(document.documentElement);
};

// Clears element selection
NetBeans.clearSelection = function() {
    for (var i=0; i<this.selection.length; i++) {
        // Restore the original style
        var selection = this.selection[i];
        selection.element.style.outline = selection.outline;
        selection.element.style.backgroundColor = selection.backgroundColor;
    }
    this.selection = [];
};

// Determines whether the given value is an array
NetBeans.isArray = function(value) {
    return (typeof(value) === 'object') && (value instanceof Array);
};

// Returns the specified element. The element specification is an array
// of numbers determining the path from the document element. For example,
// [3,1] means the (1+1)-nd child of the (3+1)-th child of the document element
// (the numbers are 0-based; that's the reason for +1 in the example above).
NetBeans.getElement = function(elementSpec) {
    var findElement = function(context, indices, idx) {
        if (indices.length === idx) {
            return context;
        } else {
            var newContext = context.childNodes[indices[idx]];
            return findElement(newContext, indices, idx+1);
        }
    };
    var element;
    if (this.isArray(elementSpec)) {
        element = findElement(document.documentElement, elementSpec, 0);
    } else {
        element = elementSpec;
    }
    return element;
};

// Adds the specified element into the selection. The element specification
// is the same as in getElement() method.
NetBeans.addElementToSelection = function(elementSpec) {
    var element = this.getElement(elementSpec);
    // Store the original style
    this.selection.push({
        element:element,
        outline:element.style.outline,
        backgroundColor:element.style.backgroundColor
    });
    // Change style to highlight the element
    element.style.outline = 'red solid 2px';
    element.style.backgroundColor = 'red';
};

// Sets the element selection according to the specified argument that is
// an array of element specifications (as described by getElement() method).
NetBeans.selectElements = function(arrayOfIndices) {
    this.clearSelection();
    for (var i=0; i<arrayOfIndices.length; i++) {
        var indices = arrayOfIndices[i];
        this.addElementToSelection(indices);
    }
};

// Returns attributes of the specified element
NetBeans.getAttributes = function(elementSpec) {
    var element = this.getElement(elementSpec);
    var attrs = element.attributes;
    var result = new Object();
    for (var i=0; i<attrs.length; i++) {
        var attr = attrs[i];
        if (attr.specified) {
            result[attr.name] = attr.value;
        }
    }
    return result;
};

// Returns computed style of the specified element
NetBeans.getComputedStyle = function(elementSpec) {
    var element = this.getElement(elementSpec);
    var style;
    if (document.defaultView && document.defaultView.getComputedStyle) {
        style = document.defaultView.getComputedStyle(element);
    } else {
        // IE 9+ in quirks mode or IE 8-
        style = element.currentStyle;
    }
    var result = new Object();
    for (var i=0; i<style.length; i++) {
        var name = style.item(i);
        var value = style.getPropertyValue(name);
        result[name] = value;
    }
    return result;
};

// Returns information about resources (scripts, images, style sheets) used
// by the inspected page.
NetBeans.getResources = function() {
    var resources = [];
    // Location
    resources.push({
        type: 'html',
        url: document.URL
    });
    // Stylesheets
    var i;
    var url;
    for (i=0; i<document.styleSheets.length; i++) {
        url = document.styleSheets[i].href;
        if (url !== null && url.length !== 0) {
            resources.push({
                type: 'styleSheet',
                url: this.removeReloadParameter(url)
            });
        }
    }
    // Scripts
    for (i=0; i<document.scripts.length; i++) {
        url = document.scripts[i].src;
        if (url !== null && url.length !== 0) {
            resources.push({
                type: 'script',
                url: this.removeReloadParameter(url)
            });
        }
    }
    // Images
    for (i=0; i<document.images.length; i++) {
        url = document.images[i].src;
        if (url !== null && url.length !== 0) {
            resources.push({
                type: 'image',
                url: this.removeReloadParameter(url)
            });
        }
    }    
    
    return resources;
}

// Helper method that removes "reload" parameter from the specified URL
NetBeans.removeReloadParameter = function(url) {
    // We can assume that the reload parameter is the last one
    // because we are putting it at the end.
    var idx = url.lastIndexOf('?'+this.RELOAD_PARAM + '=');
    if (idx != -1) {
        url = url.substring(0,idx);
    }
    idx = url.lastIndexOf('&'+this.RELOAD_PARAM + '=');
    if (idx != -1) {
        url = url.substring(0,idx);
    }
    return url;
}

// Helper method that adds "reload" parameter to the specified URL
NetBeans.addReloadParameter = function(url) {
    // Add the reload parameter
    idx = url.indexOf('?')
    if (idx == -1) {
        // No query string
        url += '?';
    } else {
        // Some query string
        url += '&';
    }
    url += this.RELOAD_PARAM + '=' + new Date().getTime();
    return url;
}

// Forces reload of the style sheet with the given URL
NetBeans.reloadCSS = function(url) {
    var found = false;
    url = this.removeReloadParameter(url);
    for (var i=0; i<document.styleSheets.length; i++) {
        var node = document.styleSheets[i].ownerNode;
        var href = node.href;
        if (href != null) {
            href = this.removeReloadParameter(href);
            if (href === url) {
                // This is the stylesheet that should be reloaded
                found = true;
                var newURL = this.addReloadParameter(url);
                node.href = newURL;
            }
        }
    }
    if (!found) {
        console.log('Cannot find the style sheet to reload: '+url);
    }
}

// Forces reload of the images with the given URL
NetBeans.reloadImage = function(url) {
    var found = false;
    url = this.removeReloadParameter(url);
    for (var i=0; i<document.images.length; i++) {
        var node = document.images[i];
        var src = node.src;
        if (src != null) {
            src = this.removeReloadParameter(src);
            if (src === url) {
                // This is the image that should be reloaded
                found = true;
                var newURL = this.addReloadParameter(url);
                node.src = newURL;
            }
        }
    }
    if (!found) {
        console.log('Cannot find the image to reload: '+url);
    }
}

// Forces reload and execution of the script with the given URL
NetBeans.reloadScript = function(url) {
    var found = false;
    url = this.removeReloadParameter(url);
    for (var i=0; i<document.scripts.length; i++) {
        var node = document.scripts[i];
        var src = node.src;
        if (src != null) {
            src = this.removeReloadParameter(src);
            if (src === url) {
                // This is the script that should be reloaded
                found = true;
                
                // Simple url modification doesn't trigger script
                // execution. Happily, the execution is triggered
                // when a new script tag is added.

                // We are using our own cloning method because node.cloneNode()
                // seems to copy also some kind of flag determining whether
                // the script was executed already or not. In other words,
                // script cloned using standard cloneNode() is not executed
                // when it is added.
                var newNode = document.createElement(node.tagName);
                for (var j=0; j<node.attributes.length; j++) {
                    var attr = node.attributes[j];
                    newNode.setAttribute(attr.name, attr.value);
                }
                newNode.innerHTML = node.innerHTML;

                // Modify src attribute to avoid caching
                var newURL = this.addReloadParameter(url);
                newNode.src = newURL;

                // Replace the old node by a cloned one.
                // This should trigger script execution.
                node.parentNode.replaceChild(newNode, node);
            }
        }
    }
    if (!found) {
        console.log('Cannot find the script to reload: '+url);
    }
}
