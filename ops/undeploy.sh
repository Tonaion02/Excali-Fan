# T:  Include parameters 
source ./parameters.sh

# T:  WARNING: Deleting the resource group cause the deletion of
# T:  all the resources that are contained in the resource group.

# T:  Delete the resource group for SignalR
az group delete --name "$resource_group_signalr" --yes --no-wait

# T:  Delete the resource group for Blob Storage
az group delete --name "$resource_group_storage" --yes --no-wait

# T:  Delete the resource group fro App Service
az group delete --name "$resource_group_app_service" --yes --no-wait

# T:  Delete the resource group for keyvault
az group delete --name "$resource_group_key_vault" --yes --no-wait

# T:  Delete the resource group for Azure Functions
az group delete --name "$resource_group_function_app_service" --yes --no-wait

# T:  Remove from the azure functions the constants file
rm ../functions/src/main/java/com/fabrikam/Constants.java

# T:  Remove from the azure app service the .env file
rm  ../webapp/src/main/resources/.env

# T:  Remove from the single page application the file where the constants are contained
rm ../webapp/src/main/resources/static/constants.js