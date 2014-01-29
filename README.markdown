# Methode API
Methode API is a Dropwizard application which allows for obtaining assets from Methode, as well as their types.

## Introduction
This application will connect to a running Methode instance - its resolution depends on your DNS settings.

## Running
In order to run the project, please run com.ft.methodeapi.MethodeApiService with the following program parameters:
server methode-api.yaml

Please make sure you are running it in the correct working directory (methode-api-service).

Healthcheck: [http://localhost:9081/healthcheck]

## Using the API
Hit http://localhost:9080/eom-file/42e3d9b6-187e-11e3-83b9-00144feab7de
You don't need to provide any headers.

## Running acceptance tests
For all acceptance tests, run com.ft.methodeapi.acceptance.AllTests the with the following VM argument:
-Dtest.methodeApi.configFile=local.yaml

You can also run smoke tests with the same VM argument by running com.ft.methodeapi.acceptance.SmokeTests