# Excali-Fun

Excali-Fun is a webapp inspired to Excalidraw: https://excalidraw.com/ 
Excali-Fun is a webapp application that allows multiple clients to draw on a virtual board in a real time.
Excali-Fun also allows to save to save boards in a storage.  

## Create resources and configure services

## Run the application

## Services used
- Azure Web App: It is used to run the web-app server.
- Azure SignalR: It is used to lighten the server load by handling client WebSocket connections and sending messages to clients within the same group.
- Azure EntraId: It is used to manage user authentication. It allows users to log in using a Microsoft account.
- Azure KeyVault: It is used to manage the secrets that are used by other services(Azure SignalR accountKey, Azure Blob Storage accountKey...)
- Azure Blob Storage: It is used to persist boards in memory.
- Azure Function: It is used to handle some operations like downloading and uploading of the boards. 

## Project Architecture
![architecture_image](doc/arc.png)

## Struttura della repository