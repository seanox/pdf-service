# Development


## Contents Overview
* [Publish a release](#publish-a-release)
   * [GitHub](#github)
   * [Maven Repository](#maven-repository)


## Publish a release

### GitHub
- Start in the project directory
- Search and solve all TODO entries
- Check and if necessary update the version and time stamp of all components
  comparison with the tag from the last release.
- Maven: check and update of dependencies
- Final test  
  Call `mvn clean test`
- __Provided all tests are successful!__
- Final update of CHANGES / README.md / pom.xml / PDF-Tools  
  incl. the Maven-dependency version and update the version in `pom.xml`  
  Call `ant -f ./development/build.xml release`  
  The versions in the classes should be updated.
- Final commit of the release  
  Release x.x.x
- Create tag with the short form of the release notes  
  first line: Version x.x.x xxxxxxxx

### Maven Repository
- __Based on the previous step__
- Call `ant -f ./development/build.xml release`  
  Ignore the changes, after that rollback can be used  
  Call `ant -f ./development/build.xml clean deploy`  
  see also https://oss.sonatype.org/#nexus-search;quick~com.seanox  
  see also https://mvnrepository.com/artifact/com.seanox/seanox-pdf-service
