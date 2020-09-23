# isprediksjon
Application written in Kotlin used to store sykmeldinger and extra info to be used for prediksjoner about the length of a syketilfelle.

## Technologies used
* Kotlin
* Ktor
* Gradle
* JDK 11
* Spek
* Jackson

#### Requirements

* JDK 11

#### Build and run tests
To build locally and run the integration tests you can simply run `./gradlew shadowJar` or on windows 
`gradlew.bat shadowJar`

#### Creating a docker image
Creating a docker image should be as simple as `docker build -t isprediksjon .`

#### Running a docker image
`docker run --rm-it -p 8080:8080 isprediksjon`


## Contact us
### Code/project related questions can be sent to
* June Henriksen, `june.henriksen2@nav.no`
* Mathias Rørvik, `mathias.fris.rorvik@nav.no`
* John Martin Lindseth `john.martin.lindseth@nav.no`
* The following channel on slack --> #isyfo 
