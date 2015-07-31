# df-xpath

The df-xpath tree is an Actian DataFlow operator for querying XML data.

## Configuration

Before building df-xpath you need to define the following environment variables to point to the local DataFlow update site [dataflow-p2-site](https://github.com/ActianCorp/dataflow-p2-site) root directory and the DataFlow version.

    export DATAFLOW_REPO_HOME=/Users/myuser/dataflow-p2-site
    export DATAFLOW_VER=6.5.2.112

## Building

The update site is built using [Apache Maven 3.0.5 or later](http://maven.apache.org/).

To build, run:

    mvn clean install
    
You can update the version number by running

    mvn org.eclipse.tycho:tycho-versions-plugin:set-version -DnewVersion=version
    
where version is of the form x.y.z or x.y.z-SNAPSHOT.
    

## Using the XPath operator with the DataFlow Engine

The build generates a JAR file in the target directory under
[df-xpath/DataflowExtensions](https://github.com/ActianCorp/df-xpath/tree/master/DataflowExtensions)
with a name similar to 

    xpath-dataflow-1.y.z.jar

which can be included on the classpath when using the DataFlow engine.

## Installing the XPath operator in KNIME

The build also produces a ZIP file which can be used as an archive file with the KNIME 'Help/Install New Software...' dialog.
The ZIP file can be found in the target directory under
[df-xpath/KnimeExtensions/Knime-Update](https://github.com/ActianCorp/df-xpath/tree/master/KnimeExtensions/Knime-Update) 
and with a name like 


    com.actian.services.knime.xpath.update-1.y.z.zip
 
## Limitations and Reservations

Expressions are limited to XPath 1.0 expressions.

Only tested against DF 6.5.1.115 and KNIME 2.9.4 on Windows 7