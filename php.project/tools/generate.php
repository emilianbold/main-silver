<?php
/**
 * This script can be used for generating PHP model for PDT.
 * It builds PHP functions according to the loaded extensions in running PHP,
 * using complementary information gathered from PHP.net documentation
 *
 * @author Michael Spector <michael@zend.com>
 */

define("DOC_URL", "./html/");      // PHP documentation, separate HTML files
if (!is_dir(DOC_URL)) {
    die('Incorrect directory for separated HTML files ("./html/" expected)!');
}

if (version_compare(phpversion(), "5.0.0") < 0) {
	die ("This script requires PHP 5.0.0 or higher!\n");
}

$splitFiles = true;
$phpdocDir = null;

$phpDir = "php5";
if (strstr(phpversion(), "5.3")) {
	$phpDir = "php5.3";
}

// Parse arguments:
$argv = $_SERVER["argv"];
$argv0 = array_shift ($argv);
for ($i = 0; $i < count($argv); ++$i) {
	switch ($argv[$i]) {
		case "-nosplit":
			$splitFiles = false;
			break;

		case "-help":
			show_help();
			break;

		default:
			$phpdocDir = $argv[$i];
	}
}

if (!$phpdocDir) {
	show_help();
}
$extensions = get_loaded_extensions();
$functionsDoc = parse_phpdoc_functions ($phpdocDir, $extensions);
$fieldsDoc = parse_phpdoc_fields ($phpdocDir, $extensions);
$classesDoc = parse_phpdoc_classes ($phpdocDir, $extensions);
$constantsDoc = parse_phpdoc_constants ($phpdocDir);

$processedFunctions = array();
$processedClasses = array();
$processedConstants = array();

@mkdir ($phpDir);

if (!$splitFiles) {
	begin_file_output();
}
foreach ($extensions as $extName) {
	if ($splitFiles) {
		begin_file_output();
	}
	print_extension (new ReflectionExtension ($extName));
	if ($splitFiles) {
		finish_file_output("{$phpDir}/{$extName}.php");
	}
}

if ($splitFiles) {
	begin_file_output();
}
$intFunctions = get_defined_functions();
foreach ($intFunctions["internal"] as $intFunction) {
	if (!@$processedFunctions[strtolower($intFunction)]) {
		print_function (new ReflectionFunction ($intFunction));
	}
}

$intClasses = array_merge (get_declared_classes(), get_declared_interfaces());
foreach ($intClasses as $intClass) {
	if (!@$processedClasses[strtolower($intClass)]) {
		print_class (new ReflectionClass ($intClass));
	}
}

print "\n";
$constants = get_defined_constants(true);
$intConstants = isset($constants["internal"]) ? $constants["internal"] : array();
// add magic constants:
$intConstants['__FILE__'] = null;
$intConstants['__LINE__'] = null;
$intConstants['__CLASS__'] = null;
$intConstants['__FUNCTION__'] = null;
$intConstants['__METHOD__'] = null;
if (version_compare(phpversion(), "5.3.0") >= 0) {
	$intConstants['__DIR__'] = null;
	$intConstants['__NAMESPACE__'] = null;
}
foreach ($intConstants as $name => $value) {
	if (!@$processedConstants[$name]) {
		print_constant ($name, $value);
	}
}


finish_file_output("{$phpDir}/basic.php");

// Create .list file
$fp = fopen ("{$phpDir}/.list", "w");
foreach (glob("{$phpDir}/*.php") as $f) {
	fwrite ($fp, basename($f));
	fwrite ($fp, "\n");
}
fclose($fp);


function findVerInfo($file)
 {
    $url = DOC_URL.$file.".html";
    $search_for = '<p class="verinfo">';
    //echo "Reading $url :\n";

    if (!is_file($url)) {
        return;
    }

    $file_contents = file_get_contents($url);

    $start_pos = strpos($file_contents, $search_for);

    if ($start_pos !== 0) {
       $start_pos += strlen($search_for);
       $end_pos = strpos($file_contents, '</p>', $start_pos);

       if ($end_pos !== 0) {
          $verinfo = substr($file_contents, $start_pos, $end_pos - $start_pos);
          //echo "Ver. info: $verinfo\n";
          return $verinfo;
       }
    }
 }

// === Functions ===
/**
 * Makes generic key from given function name
 * @param name string Function name
 * @return string generic key
 */
function make_funckey_from_str ($name) {
	$name = str_replace ("->", "::", $name);
	$name = str_replace ("()", "", $name);
	$name = strtolower ($name);
	return $name;
}

/**
 * Replaces all invalid charaters with '_' in PHP identifier
 * @param name PHP identifier
 * @return string PHP identifier with stripped invalid characters
 */
function clean_php_identifier ($name) {
	$name = preg_replace('/[^\$\w\_]+/', '_', $name);
	return $name;
}

/**
 * Makes generic key from given function reference
 * @param name ReflectionMethod function reference
 * @return string generic key
 */
function make_funckey_from_ref ($ref) {
	if ($ref instanceof ReflectionMethod) {
		$funckey = make_classmember_ref($ref->getDeclaringClass()->getName(), $ref->getName());
	} else {
		$funckey = strtolower($ref->getName());
	}
	return $funckey;
}
function make_property_from_ref ($ref) {
	if ($ref instanceof ReflectionProperty) {
		$funckey = make_classmember_ref($ref->getDeclaringClass()->getName(), $ref->getName());
	} else {
		throw new Exception("Unexpected type: ".gettype($ref));
	}
	return $funckey;
}

function make_classmember_ref ($className, $memberName) {
	return strtolower($className)."::".strtolower($memberName);
}


/**
 * Parses PHP documentation
 * @param phpdocDir string PHP.net documentation directory
 * @return array Function information gathered from the PHP.net documentation by parsing XML files
 */
function parse_phpdoc_functions ($phpdocDir, $extensions) {
	$xml_files = array_merge (
		glob ("{$phpdocDir}/en/reference/*/functions/*.xml"),
		glob ("{$phpdocDir}/en/language/predefined/*/*.xml"),
		glob ("{$phpdocDir}/en/reference/*/functions/*/*.xml")
	);
	foreach ($extensions as $extName) {
		$extName = strtolower($extName);
		$globPattern = "{$phpdocDir}/en/reference/{$extName}/*/*.xml";
		$xml_files = array_merge (
			$xml_files,
			glob ($globPattern)
		);
	}
    $functionsDoc = array();
	foreach ($xml_files as $xml_file) {
		$xml = file_get_contents ($xml_file);

		if (preg_match ('@<refentry.*?xml:id=["\'](.*?)["\'].*?>.*?<refname>(.*?)</refname>.*?<refpurpose>(.*?)</refpurpose>@s', $xml, $match)) {

			$refname = make_funckey_from_str ($match[2]);
            $functionsDoc[$refname] = array();
            $functionsDoc[$refname]['id'] = $match[1];
            $functionsDoc[$refname]['quickref'] = trim($match[3]);

			if (preg_match ('@<refsect1\s+role=["\']description["\']>(.*?)</refsect1>@s', $xml, $match)) {
				$description = $match[1];
				$function_alias = null;
				$parameters = null;
				$has_object_style = false;
				if (preg_match ('@^(.*?)<classsynopsis>.*?<classname>(.*)</classname>.*?<methodsynopsis>.*?<type>(.*?)</type>.*?<methodname>(.*?)</methodname>(.*?)</methodsynopsis>.*?</classsynopsis>(.*)$@s', $description, $match)) {
					$functionsDoc[$refname]['classname'] = trim($match[2]);
					$functionsDoc[$refname]['returntype'] = trim($match[3]);
					$functionsDoc[$refname]['methodname'] = trim($match[4]);
					$parameters = $match[5];
					$description = $match[1].$match[6];
					$has_object_style = true;
				}
                                $methodsynopsis;
                                if ($refname == 'number_format') {
                                    $methodsynopsis = preg_match_all ('@<methodsynopsis>.*?<type>(.*?)</type>.*?<methodname>(.*?)</methodname>(.*?)</methodsynopsis>@s', $description, $tmp);
                                    $match = array();
                                    foreach ($tmp as $key => $val) {
                                        $match[$key] = $val[count($val) - 1];
                                    }
                                } else {
                                    $methodsynopsis = preg_match ('@<methodsynopsis>.*?<type>(.*?)</type>.*?<methodname>(.*?)</methodname>(.*?)</methodsynopsis>@s', $description, $match);
                                }
				if ($methodsynopsis) {
					if ($has_object_style) {
						$function_alias = trim($match[2]);
					} else {
						$functionsDoc[$refname]['returntype'] = trim($match[1]);
						$functionsDoc[$refname]['methodname'] = trim($match[2]);
                                                $parameters = $match[3];
					}
				}
				if ($parameters) {
					if (preg_match_all ('@<methodparam\s*(.*?)>.*?<type>(.*?)</type>.*?<parameter\s*(.*?)>(.*?)</parameter>(?:<initializer>(.+?)</initializer>)?.*?</methodparam>@s', $parameters, $match)) {
						for ($i = 0; $i < count($match[0]); ++$i) {
							$parameter = array (
								'type' => trim(str_replace('-', '_', $match[2][$i])), // e.g. OCI-Collection -> OCI_Collection
								'name' => clean_php_identifier(trim($match[4][$i])),
							);
							if (preg_match ('@choice=[\'"]opt[\'"]@', $match[1][$i])) {
								$parameter['isoptional'] = true;
							}
							if (preg_match ('@role=[\'"]reference[\'"]@', $match[3][$i])) {
								$parameter['isreference'] = true;
							}
							if (@strlen(trim($match[5][$i]))) {
								$parameter['defaultvalue'] = trim($match[5][$i]);
                                                                $parameter['isoptional'] = true;
							}
							$functionsDoc[$refname]['parameters'][] = $parameter;
						}
					}
				}
			}
			if (preg_match ('@<refsect1\s+role=["\']parameters["\']>(.*?)</refsect1>@s', $xml, $match)) {
				$parameters = $match[1];
                if (preg_match_all('@<varlistentry\s*.*?>.*?<parameter>(.*?)</parameter>.*?<listitem\s*.*?>(.*?)</listitem>.*?</varlistentry>@s', $parameters, $match)) {
                    for ($i = 0; $i < count($match[0]); $i++) {
                        for ($j = 0; $j < count(@$functionsDoc[$refname]['parameters']); $j++) {
                            if (clean_php_identifier(trim($match[1][$i])) == $functionsDoc[$refname]['parameters'][$j]['name']) {
                                $functionsDoc[$refname]['parameters'][$j]['paramdoc'] = xml_to_phpdoc ($match[2][$i]);
                                break;
                            }
                        }
                    }
                }
			}
			if (preg_match ('@<refsect1\s+role=["\']returnvalues["\']>(.*?)</refsect1>@s', $xml, $match)) {
				$returnvalues = $match[1];
				if (preg_match ('@<para>\s*(Returns)?(.*)</para>?@s', $returnvalues, $match)) {
					$functionsDoc[$refname]['returndoc'] = xml_to_phpdoc ($match[2]);
				}
			}

			// Create information for function alias
			if ($function_alias) {
				$functionsDoc[$function_alias] = $functionsDoc[$refname];
			}
		}
	}
	return $functionsDoc;
}

/**
 * Parses PHP documentation
 * @param phpdocDir string PHP.net documentation directory
 * @return array Function information gathered from the PHP.net documentation by parsing XML files
 */
function parse_phpdoc_fields ($phpdocDir, $extensions) {
	$xml_files = array();
	foreach ($extensions as $extName) {
		$extName = strtolower($extName);

		$xml_files = array_merge (
			$xml_files,
			glob ("{$phpdocDir}/en/reference/{$extName}/*.xml"),
                        glob ("{$phpdocDir}/en/reference/{$extName}/*/*.xml")
		);
	}
        foreach ($xml_files as $xml_file) {
            $xml = file_get_contents($xml_file);
            if (preg_match('@<classsynopsisinfo>.*?<classname>(.*)</classname>.*?</classsynopsisinfo>@s', $xml, $matchOffset, PREG_OFFSET_CAPTURE)) {
                $classname = $matchOffset[1][0];
                if (preg_match_all("@<fieldsynopsis>.*?<type>(.*?)</type>.*?<varname.*?>(.*?)</varname>@s", $xml, $matchOffset,null,$matchOffset[1][1])) {
                    for ($index = 0; $index < count($matchOffset[2]); $index++) {
                        $name = $matchOffset[2][$index];
                        $type= $matchOffset[1][$index];
                        $exploded = explode("::", $name);
                        if (count($exploded) > 1) {
                            $name = $exploded[1];
                        }
                        $reference = make_classmember_ref($classname, $name);
                        $fieldsDoc[$reference]['field'] = $name;
                        $fieldsDoc[$reference]['type'] = $type;
                    }
                }
            } else {
                if (preg_match('@<classsynopsis>.*?<classname>(.*)</classname>.*?<fieldsynopsis>.*?<type>(.*?)</type>.*?<varname.*?>(.*?)</varname>.*?</classsynopsis>@s', $xml, $match)) {
                    $reference = make_classmember_ref($match[1], $match[3]);
                    $fieldsDoc[$reference]['field'] = $match[3];
                    $fieldsDoc[$reference]['type'] = $match[2];
                    //$fieldsDoc[$refname]['quickref'] = trim($match[3]);
                }
            }

        }
        if (isset($fieldsDoc)) {
            return $fieldsDoc;
        }
        return array();
}

/**
 * Parses PHP documentation
 * @param phpdocDir string PHP.net documentation directory
 * @return array Class information gathered from the PHP.net documentation by parsing XML files
 */
function parse_phpdoc_classes ($phpdocDir, $extensions) {
	$xml_files = array_merge (
		glob ("{$phpdocDir}/en/reference/*/reference.xml"),
		glob ("{$phpdocDir}/en/reference/*/classes.xml"),
		glob ("{$phpdocDir}/en/language/*/*.xml"),
		glob ("{$phpdocDir}/en/language/*.xml")
	);
	foreach ($extensions as $extName) {
		$extName = strtolower($extName);
		$globPattern = "{$phpdocDir}/en/reference/{$extName}/*.xml";
		$xml_files = array_merge (
			$xml_files,
			glob ($globPattern)
		);
	}

    $classesDoc = array();
	foreach ($xml_files as $xml_file) {
		$xml = file_get_contents ($xml_file);
		if (preg_match ('@xml:id=["\'](.*?)["\']@', $xml, $match)) {
			$id = $match[1];
			$prefixId = substr($id, 0, strlen("class."));
			$clsNamePattern = ($prefixId === "class.") ?
			'@<ooclass><classname>(.*?)</classname></ooclass>@' :
			'@<title><classname>(.*?)</classname></title>@';
			if (preg_match_all ($clsNamePattern, $xml, $match)) {
				for ($i = 0; $i < count($match[0]); ++$i) {
					$class = $match[1][$i];
					$refname = strtolower ($class);
					$classesDoc[$refname]['id'] = $id;
					$classesDoc[$refname]['name'] = $class;
					$offsetPattern = ($prefixId === "class.") ?
						"@xml:id=[\"'](.*?)\.intro[\"']@i" :
						"@<title><classname>{$class}</classname></title>@";
					if (preg_match ($offsetPattern , $xml, $matchOffset,PREG_OFFSET_CAPTURE)) {
						$docPattern = '@<para>(.*?)</para>@s';
						if (preg_match ($docPattern, $xml, $match2,null,$matchOffset[0][1])) {
							$doc = xml_to_phpdoc($match2[1]);
							$classesDoc[$refname]['doc'] = $doc;
						}
					}
				}
			}
		}
	}
	return $classesDoc;
}

/**
 * Parses PHP documentation
 * @param phpdocDir string PHP.net documentation directory
 * @return array Constant information gathered from the PHP.net documentation by parsing XML files
 */
function parse_phpdoc_constants ($phpdocDir) {
	exec ("find ".addslashes($phpdocDir)." -name \"*constants.xml\"", $xml_files);
	foreach ($xml_files as $xml_file) {
		$xml = file_get_contents ($xml_file);

		if (preg_match ('@xml:id=["\'](.*?)["\']@', $xml, $match)) {
			$id = $match[1];
			if (preg_match_all ('@<term>\s*<constant>([a-zA-Z_:][a-zA-Z0-9_:]*)</constant>.*?</term>.*?<listitem>(.*?)</listitem>@s', $xml, $match)) {
				for ($i = 0; $i < count($match[0]); ++$i) {
					$constant = $match[1][$i];
					$constantsDoc[$constant]['id'] = $id;
					$constantsDoc[$constant]['doc'] = xml_to_phpdoc($match[2][$i]);
				}
			}
			if (preg_match_all (
				'@<entry>\s*<constant>([a-zA-Z_][a-zA-Z0-9_]*)</constant>.*?</entry>\s*<entry>\d+</entry>\s*<entry>(.*?)</entry>@s', $xml, $match)
				|| preg_match_all ('@<entry>\s*<constant>([a-zA-Z_][a-zA-Z0-9_]*)</constant>.*?</entry>\s*<entry>(.*?)</entry>@s', $xml, $match)) {

				for ($i = 0; $i < count($match[0]); ++$i) {
					$constant = $match[1][$i];
					$constantsDoc[$constant]['id'] = $id;
					$constantsDoc[$constant]['doc'] = xml_to_phpdoc($match[2][$i]);
				}
			}
		}
	}
	return $constantsDoc;
}

/**
 * Prints ReflectionExtension in format of PHP code
 * @param extRef ReflectionExtension object
 */
function print_extension ($extRef) {
	print "\n// Start of {$extRef->getName()} v.{$extRef->getVersion()}\n";

	// process classes:
	$classesRef = $extRef->getClasses();
	if (count ($classesRef) > 0) {
		foreach ($classesRef as $classRef) {
			print_class ($classRef);
		}
	}

	// process functions
	$funcsRef = $extRef->getFunctions();
	if (count ($funcsRef) > 0) {
		foreach ($funcsRef as $funcRef) {
			print_function ($funcRef);
		}
		print "\n";
	}

	// process constants
	$constsRef = $extRef->getConstants();
	if (count ($constsRef) > 0) {
		print_constants ($constsRef);
		print "\n";
	}

	print "// End of {$extRef->getName()} v.{$extRef->getVersion()}\n";
}

/**
 * Prints ReflectionClass in format of PHP code
 * @param classRef ReflectionClass object
 * @param tabs integer[optional] number of tabs for indentation
 */
function print_class ($classRef, $tabs = 0) {
	global $processedClasses;
	$processedClasses [strtolower($classRef->getName())] = true;

	print "\n";
	print_doccomment ($classRef, $tabs);
	print_tabs ($tabs);
	if ($classRef->isFinal()) print "final ";

	print $classRef->isInterface() ? "interface " : "class ";
	print clean_php_identifier($classRef->getName())." ";

	// print out parent class
	$parentClassRef = $classRef->getParentClass();
	if ($parentClassRef) {
		print "extends {$parentClassRef->getName()} ";
	}

	// print out interfaces
	$interfacesRef = $classRef->getInterfaces();
	if (count ($interfacesRef) > 0) {
		print $classRef->isInterface() ? "extends " : "implements ";
		$i = 0;
		foreach ($interfacesRef as $interfaceRef) {
			if ($i++ > 0) {
				print ", ";
			}
			print "{$interfaceRef->getName()}";
		}
	}
	print " {\n";

	// process constants
	$constsRef = $classRef->getConstants();
	if (count ($constsRef) > 0) {
		print_class_constants ($classRef, $constsRef, $tabs + 1);
		print "\n";
	}

	// process properties
	$propertiesRef = $classRef->getProperties();
	if (count ($propertiesRef) > 0) {
		foreach ($propertiesRef as $propertyRef) {
			print_property ($propertyRef, $tabs + 1);
		}
		print "\n";
	}

	// process methods
	/* @var $classRef ReflectionClass */
	$methodsRef = $classRef->getMethods();
	if (count ($methodsRef) > 0) {
		foreach ($methodsRef as $methodRef) {
            /* @var $methodRef ReflectionMethod */
            if ($methodRef->getName() == 'clone') {
                continue;
            }
			print_function ($methodRef, $tabs + 1);
		}
		print "\n";
	}
	print_tabs ($tabs);
	print "}\n";
}

/**
 * Prints ReflectionProperty in format of PHP code
 * @param ReflectionProperty $propertyRef  object
 * @param integer[optional] tabs  number of tabs for indentation
 */
function print_property ($propertyRef, $tabs = 0) {
	print_doccomment ($propertyRef, $tabs);
	print_tabs ($tabs);
	print_modifiers ($propertyRef);
	print "\${$propertyRef->getName()};\n";
}

function print_function ($functionRef, $tabs = 0) {
	global $functionsDoc;
	global $processedFunctions;

	$funckey = make_funckey_from_ref ($functionRef);
	$processedFunctions[$funckey] = true;

	print "\n";
	print_doccomment ($functionRef, $tabs);
	print_tabs ($tabs);
	if (!($functionRef instanceof ReflectionFunction)) {
		print_modifiers ($functionRef);
	}

	print "function ";
	if ($functionRef->returnsReference()) {
		print "&";
	}
	print "{$functionRef->getName()} (";
	$parameters = @$functionsDoc[$funckey]['parameters'];
	if ($parameters) {
		print_parameters ($parameters);
	} else {
		print_parameters_ref ($functionRef->getParameters());
	}
	print ") {}\n";
}


/**
 * Prints ReflectionParameter in format of PHP code
 * @param parameters array information from PHP.net documentation
 */
function print_parameters ($parameters) {
	$i = 0;
	foreach ($parameters as $parameter) {
		if ($parameter['name'] != "...") {
			if ($i++ > 0) {
				print ", ";
			}
			$type = $parameter['type'];
			if ($type && !in_array($type, array(
                            'mixed',
                            'string',
                            'int',
                            'bool',
                            'object',
                            'callback',
                            'resource',
                            'string|array',
                            'bitmask',
                            'name',
                            'number',
                            'float',
                            'string|int',
                        ))) {
				print "{$type} ";
			}
			if (@$parameter['isreference']) {
				print "&";
			}
                        print "\${$parameter['name']}";

			if (@$parameter['isoptional']) {
				if (@strlen($parameter['defaultvalue'])) {
					$value = $parameter['defaultvalue'];
                                        if (is_numeric ($value)
                                                || in_array(strtolower($value), array('true', 'false', '&null'))
                                                || (substr($value, 0, 1) == '\'' && substr($value, -1) == '\'')
                                                || (substr($value, 0, 1) == '"' && substr($value, -1) == '"')) {
                                            // no apostrophes
                                        } else {
                                            $value = "'{$value}'";
                                        }
                                        print " = {$value}";
				} else {
					print " = null";
				}
			}
		}
	}
}

/**
 * Prints ReflectionParameter in format of PHP code
 * @param paramsRef ReflectionParameter[] array of objects
 */
function print_parameters_ref ($paramsRef) {
	$i = 0;
	foreach ($paramsRef as $paramRef) {
		if ($paramRef->isArray()) {
			print "array ";
		} else {
			if ($className = get_parameter_classname($paramRef)) {
				print "{$className} ";
			}
		}
		$name = $paramRef->getName() ? $paramRef->getName() : "var".($i+1);
		if ($name != "...") {
			if ($i++ > 0) {
				print ", ";
			}
			if ($paramRef->isPassedByReference()) {
				print "&";
			}
			print "\${$name}";
			if ($paramRef->allowsNull()) {
				print " = null";
			} else if ($paramRef->isDefaultValueAvailable()) {
				$value = $paramRef->getDefaultValue();
				if (!is_numeric ($value)) {
					$value = "'{$value}'";
				}
				print " = {$value}";
			}
		}
	}
}

/**
 * Prints constants in format of PHP code
 * @param constants array containing constants, where key is a name of constant
 * @param tabs integer[optional] number of tabs for indentation
 */
function print_constants ($constants, $tabs = 0) {
	foreach ($constants as $name => $value) {
		print_constant ($name, $value, $tabs);
	}
}

function print_constant ($name, $value = null, $tabs = 0) {
	global $constantsDoc;
	global $processedConstants;
	$processedConstants [$name] = true;

	if ($value === null) {
		$value = @constant ($name);
	}
	$value = escape_const_value ($value);

	$doc = @$constantsDoc[$name]['doc'];
	if ($doc) {
		print "\n";
		print_tabs ($tabs);
		print "/**\n";
		print_tabs ($tabs);
		print " * ".newline_to_phpdoc($doc, $tabs)."\n";
		print_tabs ($tabs);
		print " * @link ".make_url($constantsDoc[$name]['id'])."\n";
		print_tabs ($tabs);
		print " */\n";
	}
	print_tabs ($tabs);
	print "define ('{$name}', {$value});\n";
}

function escape_const_value ($value) {
	if (is_resource($value)) {
		$value = "\"${value}\"";
	} else if (!is_numeric ($value) && !is_bool ($value) && $value !== null) {
		$value = '"'.addcslashes ($value, "\"\r\n\t").'"';
	} else if ($value === null) {
		$value = "null";
	} else if ($value === false) {
		$value = "false";
	} else if ($value === true) {
		$value = "true";
	}
	return $value;
}

/**
 * Prints class constants in format of PHP code
 * @param constants array containing constants, where key is a name of constant
 * @param tabs integer[optional] number of tabs for indentation
 */
function print_class_constants ($classRef, $constants, $tabs = 0) {
    global $constantsDoc;
    global $processedConstants;


    //$doc = @$constantsDoc[$name]['doc'];
    foreach ($constants as $name => $value) {
        $value = escape_const_value ($value);
        $clsName = $classRef->getName();
        $idx = "$clsName::$name";
        $doc = @$constantsDoc[$idx]['doc'];
        if ($doc) {
            print "\n";
            print_tabs ($tabs);
            print "/**\n";
            print_tabs ($tabs);
            print " * ".newline_to_phpdoc($doc, $tabs)."\n";
            print_tabs ($tabs);
            print " * @link ".make_url($constantsDoc[$idx]['id'])."\n";
            print_tabs ($tabs);
            print " */\n";
        }
        print_tabs ($tabs);
        print "const {$name} = {$value};\n";
    }
}

/**
 * Prints modifiers of reflection object in format of PHP code
 * @param ref Reflection some reflection object
 */
function print_modifiers ($ref) {
	$modifiers = Reflection::getModifierNames ($ref->getModifiers());
	if (count ($modifiers) > 0) {
		print implode(' ', $modifiers);
		print " ";
	}
}

/**
 * Makes PHP Manual URL from the given ID
 * @param id PHP Element ID
 * @return URL
 */
function make_url ($id) {
	return "http://php.net/manual/en/{$id}.php";
}

/**
 * Prints PHPDOC comment before specified reflection object
 * @param ref Reflection some reflection object
 * @param tabs integer[optional] number of tabs for indentation
 */
function print_doccomment ($ref, $tabs = 0) {
	global $functionsDoc;
	global $classesDoc;
        global $fieldsDoc;

	$docComment = $ref->getDocComment();
	if ($docComment) {
		print_tabs ($tabs);
		print "{$docComment}\n";
	}
	else if ($ref instanceof ReflectionClass) {
		$refname = strtolower($ref->getName());
		if (@$classesDoc[$refname]) {
			print_tabs ($tabs);
			print "/**\n";
			$doc = @$classesDoc[$refname]['doc'];
			if ($doc) {
				$doc = newline_to_phpdoc ($doc, $tabs);
				print_tabs ($tabs);
				print " * {$doc}\n";
			}
			if (@$classesDoc[$refname]['id']) {
				print_Tabs ($tabs);
				$url = make_url ($classesDoc[$refname]['id']);
				print " * @link {$url}\n";
			}
			print_tabs ($tabs);
			print " */\n";
		}
	}
	else if ($ref instanceof ReflectionFunctionAbstract) {
		$funckey = make_funckey_from_ref ($ref);
		$returntype = @$functionsDoc[$funckey]['returntype'];
                $id = @$functionsDoc[$funckey]['id'];
                $ver_info = findVerInfo($id);
                $desc = @$functionsDoc[$funckey]['quickref'];
		$returndoc = newline_to_phpdoc (@$functionsDoc[$funckey]['returndoc'], $tabs);

		$paramsRef = $ref->getParameters();
		$parameters = @$functionsDoc[$funckey]['parameters'];

		if ($desc || count ($paramsRef) > 0 || $parameters || $returntype) {
			print_tabs ($tabs);
			print "/**\n";
                        if($ver_info) {
                            print_tabs ($tabs);
                            print " * {$ver_info}<br/>\n";
                        }
                        if ($desc) {
				print_tabs ($tabs);
				print " * {$desc}\n";
			}
			if (@$functionsDoc[$funckey]['id']) {
				print_tabs ($tabs);
				$url = make_url ($functionsDoc[$funckey]['id']);
				print " * @link {$url}\n";
			}
                        if($parameters) {
                            foreach ($parameters as $parameter) {
                                print_tabs($tabs);
                                print " * @param {$parameter['type']} \${$parameter['name']}";
                                if (@$parameter['isoptional']) {
                                    print " [optional]";
                                }
                                $paramdoc = @$parameter['paramdoc'];
                                if ($paramdoc && $paramdoc != "<p>\n</p>") {
                                    $paramdoc = newline_to_phpdoc(@$parameter['paramdoc'], $tabs);
                                    print " {$paramdoc}";
                                }
                                print "\n";
                            }
                        } else {
                            $i = 0;
                            foreach ($paramsRef as $paramRef) {
                                print_tabs($tabs);
                                $name = $paramRef->getName() ? $paramRef->getName() : "var".++$i;
                                print " * @param";
                                if($className = get_parameter_classname($paramRef)) {
                                    print " {$className}";
                                    if($paramRef->isArray()) {
                                        print "[]";
                                    }
                                }
                                print " \${$name}";
                                if($paramRef->isOptional()) {
                                    print " [optional]";
                                }
                                print "\n";
                            }
                        }
			if ($returntype) {
				print_tabs ($tabs);
				print " * @return " . trim("{$returntype} {$returndoc}") . "\n";
			}
			print_tabs ($tabs);
			print " */\n";
		}
	}else if ($ref instanceof ReflectionProperty) {
            $property_from_ref = make_property_from_ref($ref);
            $fieldName = @$fieldsDoc[$property_from_ref]['field'];
            $fieldType = @$fieldsDoc[$property_from_ref]['type'];
            if (isset ($fieldName) && isset ($fieldType)) {
                print_tabs ($tabs);
                print "/**\n";
                print_tabs ($tabs);
                print " * @var $fieldType\n";
                print_tabs ($tabs);
                print " */\n";
            }
        }
}

/**
 * Converts XML entities to human readable string for PHPDOC
 * @param str string
 * @return string
 */
function xml_to_phpdoc ($str) {
	$str = str_replace ("&return.success;", "Returns true on success or false on failure.", $str);
	$str = str_replace ("&return.void;", "", $str);
	$str = str_replace ("&true;", "true", $str);
	$str = str_replace ("&null;", "null", $str);
	$str = str_replace ("&false;", "false", $str);
	$str = str_replace ("&resource;", "resource", $str);
	$str = str_replace ("&style.oop;", "Oriented object style", $str);
	$str = str_replace ("&style.procedural;", "Procedural style", $str);
    $str = strip_tags_special ($str);
	$str = preg_replace ("/  */", " ", $str);
	$str = preg_replace ("/[\r\n][\t ]/", "\n", $str);
	$str = trim ($str);
	return $str;
}

/**
 * Converts newlines to PHPDOC prefixes in the given string
 * @param str string
 * @param tabs integer[optional] number of tabs for indentation
 * @return string PHPDOC string
 */
function newline_to_phpdoc ($str, $tabs = 0) {
	$str = preg_replace ("@\s*[\r\n]+@", "\n".str_repeat("\t", $tabs)." * ", $str);
	return $str;
}

/**
 * Prints specified number of tabs
 * @param tabs integer number of tabs to print
 */
function print_tabs ($tabs) {
	print str_repeat("\t", $tabs);
}

/**
 * Returns class name from given parameter reference, this method is a workaround
 * for the case when exception is thrown from getClass() when such classname does not exist.
 */
function get_parameter_classname(ReflectionParameter $paramRef) {
	try {
		if ($classRef = $paramRef->getClass()) {
			return $classRef->getName();
		}
	} catch (Exception $e) {
		if (preg_match('/Class (\w+) does not exist/', $e->getMessage(), $matches)) {
			return $matches[1];
		}
	}
	return null;
}

/**
 * Starts outputing to the new file
 */
function begin_file_output() {
	ob_start();
	print "<?php\n";
}

/**
 * Ends outputing, and dumps the output to the specified file
 * @param filename File to dump the output
 */
function finish_file_output($filename) {
	//if (file_exists ($filename)) {
	//	rename ($filename, "{$filename}.bak");
	//}
	print "?>\n";
	file_put_contents ($filename, ob_get_contents());
	ob_end_clean();
}

/**
 * Strips xml tags from the string like the standard strip_tags() function
 * would do, but also translates some of the docbook tags (such as tables
 * an paragraphs) to proper html tags
 * @param str string
 * @return string
 */
function strip_tags_special ($str) {
    // first mask and translate the tags to preseve
    $str = preg_replace ("/<(\/?)table>/", "###($1table)###", $str);
    $str = str_replace ("<row>", "###(tr valign=\"top\")###", $str);
    $str = str_replace ("</row>", "###(/tr)###", $str);
    $str = preg_replace ("/<(\/?)entry>/", "###($1td)###", $str);
    $str = preg_replace ("/<(\/?)para>/", "###($1p)###", $str);
    // now strip the remaining tags
    $str = strip_tags ($str);
    // and restore the translated ones
    $str = str_replace ("###(", "<", $str);
    $str = str_replace (")###", ">", $str);
    return $str;
}

/**
 * Prints usage help to the screen, and exits from program
 */
function show_help() {
	global $argv0;

	die (<<<EOF
USAGE: {$argv0} [options] <PHP.net documentation directory>

Where options are:

-help	Show this help.
-split	Split output to different files (one file per PHP extension).

EOF
	);
}

?>
