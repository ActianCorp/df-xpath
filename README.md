# df-xpath

The df-xpath tree is an Actian DataFlow operator for querying XML data.

## Configuration

Before df-xpath tree, you need to have installed the dataflow-p2-site repository, and amended the
dataflow.url property to point to the desktop site. (change the /ide suffix to /desktop)
e.g. http://dataflowrepo.actian.com/latest/desktop

Additionally, ensure that you have the following jars installed into your .m2 repo
    dataflow-commons.jar
    dataflow-library.jar
    dataflow-cluster.jar

This can be accomplished by building an empty project whose pom.xml is similar to
prerequisite.pom.xml

the following environment variables are setup similar to the following.

    export DATAFLOW_REPO_HOME=/Users/myuser/dataflow-p2-site
    export DATAFLOW_VER=6.5.0.117


## Building

The update site is built using [Apache Maven 3.0.5 or later](http://maven.apache.org/).

To build, run:

    mvn clean install
    

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