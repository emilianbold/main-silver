<#if comment>

  TEMPLATE DESCRIPTION:

  This is XHTML template for 'JSF Pages From Entity Beans' action. Templating
  is performed using FreeMaker (http://freemarker.org/) - see its documentation
  for full syntax. Variables available for templating are:

    entityName - name of entity being modified (type: String)
    managedBean - name of managed choosen in UI (type: String)
    managedBeanProperty - name of managed bean property choosen in UI (type: String)
    item - name of property used for dataTable iteration (type: String)
    comment - always set to "false" (type: boolean)
    entityDescriptors - list of beans describing individual entities. Bean has following properties:
        label - field label (type: String)
        name - field property name (type: String)
        dateTimeFormat - date/time/datetime formatting (type: String)
        blob - does field represents a large block of text? (type: boolean)
        relationshipOne - does field represent one to one or many to one relationship (type: boolean)
        relationshipMany - does field represent one to many relationship (type: boolean)
        id - field id name (type: String)
        required - is field optional and nullable or it is not? (type: boolean)

</#if>
<?xml version="1.0" encoding="ISO-8859-1" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:ui="http://java.sun.com/jsf/facelets"
      xmlns:h="http://java.sun.com/jsf/html"
      xmlns:f="http://java.sun.com/jsf/core">

    <ui:composition template="/template.xhtml">
        <ui:define name="title">
            <h:outputText value="View"></h:outputText>
        </ui:define>
        <ui:define name="body">
            <h:form>
                <h:panelGrid columns="2">
<#list entityDescriptors as entityDescriptor>
                    <h:outputText value="${entityDescriptor.label}:"/>
    <#if entityDescriptor.dateTimeFormat?? && entityDescriptor.dateTimeFormat != "">
                    <h:outputText value="${r"#{"}${entityDescriptor.name}${r"}"}" title="${entityDescriptor.label}">
                        <f:convertDateTime pattern="${entityDescriptor.dateTimeFormat}" />
                    </h:outputText>
    <#else>
                    <h:outputText value="${r"#{"}${entityDescriptor.name}${r"}"}" title="${entityDescriptor.label}"/>
    </#if>
</#list>
                </h:panelGrid>
                <br />
                <h:commandLink action="${r"#{"}${managedBean}${r".destroyAndView}"}" value="Destroy"/>
                <br />
                <br />
                <h:commandLink action="Edit" value="Edit"/>
                <br />
                <h:commandLink action="${r"#{"}${managedBean}${r".prepareCreate}"}" value="Create New ${entityName}" />
                <br />
                <h:commandLink action="${r"#{"}${managedBean}${r".prepareList}"}" value="Show All ${entityName} Items"/>
                <br />
                <br />
                <h:commandLink value="Index" action="/index" immediate="true" />

            </h:form>
        </ui:define>
    </ui:composition>

</html>
