# DataDogServerlessTemplate

DESCRIPTION: 
  SAM app integration for creating DataDog Eventss when DevOpsGuru creates an Insight

DOCUMENTATION

Publishing a SAM app
https://docs.aws.amazon.com/serverlessrepo/latest/devguide/serverlessrepo-quick-start.html

Template Anatomy for .yaml file
https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/sam-specification-template-anatomy.html

DataDog Java API
https://docs.datadoghq.com/api/latest/events/

SAM APP NAME: "DevOps-Guru-DataDog-Connector"

INSTRUCTIONS FOR DEPLOYING SAM APP

1. Make sure to create the secret DataDogSecretManager and fill out all 3 parameters. 
	DD_API_KEY
	DD_APP_KEY
	DD_SITE

2. Press the deploy button and everything should be functioning properly. 

3. See the below instructions if you want to customize the events to your liking

INSTRUCTIONS FOR CUSTOMIZING CODE:

1. Go to the Functions/src/main/java/aws.devopsguru.partner.datadog.event folder and you'll see a "DataDogEvent" and "DataDogEventType" file

CUSTOMIZING EVENT DETAILS
-IF you would like to customize the details of a specific event, go to the "DataDogEventType" file and look at the function createEvent you would like to change. There is condition based on the insight type. There are some comments in each section to help generally guide you but depending on what you need please view the following:

    -To add more DataDog specific details, please view the DataDog Java API for the function calls that are available in addition to the comments in the "createEvent" function which give you examples of additional fields you can populate if you so choose. 

    -To access specific details of an EventBridge event, go to the Eventbridge https://us-east-1.console.aws.amazon.com/events/home?region=us-east-1#/explore and go down to "Sample Event". Choose the event type such as "devops guru new insight open" to see the json format and the details available for you to access. From there, follow the existing examples using the jsonNode variable "input" to grab the information you desire for your event. 

AFTER YOU CUSTOMIZE YOUR CODE

-Run a "Maven build" in your IDE such as Eclipse to make a new .jar file out of your code and upload it to your Lambda function

DEPLOYING THIS CODE AS A NEW SAM APP

1. Go to the template.yaml file and add the Metadata section. Below is an example you can follow. See the documentation for publishing a SAM app for more details on the steps needed for deploying if you haven't done it before. 

Metadata:
    AWS::ServerlessRepo::Application:
        Name: DevOps-Guru-DataDog-Connector
        Description: DataDog Integration
        Author: Amazon DevOps Guru
        SpdxLicenseId: Apache-2.0
        LicenseUrl: ./LICENSE.txt
        ReadmeUrl: ./README.md
        Labels: [ 'DevOps-Guru', 'Datadog' ]
        HomePageUrl: https://aws.amazon.com/devops-guru/
        SemanticVersion: 0.0.2
        SourceCodeUrl: https://github.com/aws-samples/amazon-devops-guru-connector-datadog

