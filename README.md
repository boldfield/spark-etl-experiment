# Spark-ETL-Experiment

An experiment in using Spark as a backend for an interactive ETL system.

At the moment it isn't very useful: shows a column mapping (hardcoded to Chicago Crimes!). You can change data types, and cells will come back as "null" if it couldn't parse them.

It consists of an Elm frontend for editing these "column mappings". When you update the mapping, it generates SQL that it sends back to Spark (via a `spark-jobserver` job), and displays the results.

## Run

### 0. `brew install sbt && npm install elm -g`

### 1. Get [`spark-jobserver/spark-jobserver`](https://github.com/spark-jobserver/spark-jobserver) up & running

1. Download it as a zip (From the [releases page](https://github.com/spark-jobserver/spark-jobserver/releases); I used 0.6.1)
2. cd into it
3. In `project/Build.scala` line 66, change `compile->compile; test->test` to `compile->compile`. I was not able to build the server without this.
4. `sbt`
5. type `job-server-extra/reStart` in the SBT prompt (can't do this from the command line apparently; your server will die instantly). If successful, it'll be on `localhost:8090` (provides a nice interface)
6. Create a SQL context (will persist between jobs, saving time): `curl -d "" '127.0.0.1:8090/contexts/sql-context?context-factory=spark.jobserver.context.SQLContextFactory'`

### 2. Build the CSV Query Application

This is a Spark application which loads a CSV into a `SQLContext` and runs it.

```
# back in this repo
cd queryApplication
sbt assembly
```

### 3. Load the Query Application into the JobServer

(while still in `queryApplication`)

```
curl --data-binary @target/scala-2.10/csv-query-assembly-1.0.jar localhost:8090/jars/csv-query     
```

Our JobServer is now ready to go, since it has our application and a context to run it in.

*NB*: If you update the application jar and upload it, you'll have to restart the spark jobserver or create a new context (step 1.5) and update the Elm code (`Server.elm`, the `context` query param) to reference it. This is because jars seem to stick to contexts — after a context has used a jar once, it never pulls in updates (or something like that).

### 4. Start Elm Frontend

1. `elm reactor`
2. Navigate to `localhost:8000/Main.elm`

The UI should be there.
