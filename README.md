# Outcast Server

## Getting Started
* Before anything else, run `gradlew build` in the root directory of the project

### PostgreSQL
* Download and install [PostgreSQL](http://www.enterprisedb.com/products-services-training/pgdownload#windows)
    * Add `OutcastServer` role to server with a password
    * Create `Outcast` database
    * Assign `OutcastServer` as owner of the `Outcast` database
    * Create a `secret.json` file with the `OutcastServer` role password [(from this template)](https://gist.github.com/BrianErikson/053b03f0621549c14bac2668e7c3ae24) and place the file in `OutcastServer/res`
    
### Bower
* `cd OutcastServer/res/website`
* `bower install bootstrap#V4.0.0-alpha.2 tether angular`

## Running the Project

### From IntelliJ IDEA - Create a Run Configuration
* From in IDEA, right click on `src/main/core/OutcastServer.kt` and click `Create OutcastServer.kt...`
* Change Before Launch:
    * Remove Make
    * Add Gradle Task Build

### From CLI
* `gradlew build`
* `java -cp $KOTLIN_LIB/kotlin-runtime.jar -jar OutcastServer.jar`

### Finally...
* Once the server is running, connect to it by opening your favorite browser and navigate to `localhost` (as it is running on port 80)