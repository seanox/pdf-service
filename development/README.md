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
- Finalize version in the classes
- Finalize version in CHANGES
- Final update of CHANGES / README.md / pom.xml / PDF-Tools  
  Call `ant -f ./development/build.xml release`
  this also includes updating the version in `pom.xml`  
- Final commit of the release  
  Release x.x.x
- Create a tag without comments  

### Maven Repository
- __Based on the previous step__
- Call `ant -f ./development/build.xml publish`  
  see also https://oss.sonatype.org/#nexus-search;quick~com.seanox  
  see also https://mvnrepository.com/artifact/com.seanox/seanox-pdf-service
