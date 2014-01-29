# Methode API
Methode API is a Dropwizard application which allows for obtaining assets from Methode, as well as their types.

## Introduction
This application will connect to a running Methode instance - its resolution depends on your DNS settings.

## Running
In order to run the project, please run com.ft.methodeapi.MethodeApiService with the following program parameters:
server methode-api.yaml

Please make sure you are running it in the correct working directory (methode-api-service).

## Running acceptance tests
For all acceptance tests, run com.ft.methodeapi.acceptance.AllTests the with the following VM argument:
-Dtest.methodeApi.configFile=local.yaml

You can also run smoke tests with the same VM argument by running com.ft.methodeapi.acceptance.SmokeTests