# Include parameters 
source ./parameters.sh

# WARNING: Deleting the resource group cause the deletion of
# all the resources that are contained in the resource group.

# Delete the resource group for SignalR
az group delete --name "$resource_group_signalr" --yes --no-wait

# Delete the resource group for Blob Storage
az group delete --name "$resource_group_storage" --yes --no-wait

# Delete the resource group fro App Service
az group delete --name "$resource_group_app_service" --yes --no-wait

# Delete the resource group for keyvault
az group delete --name "$resource_group_key_vault" --yes --no-wait

# Delete the resource group for Azure Functions
az group delete --name "$resource_group_function_app_service" --yes --no-wait

# Remove from the azure functions the constants file
rm ../functions/src/main/java/com/fabrikam/Constants.java

# Remove from the azure app service the .env file
rm  ../testSignalRInSpring/my-webapp/src/main/resources/.env

# Remove from the single page application the file where the constants are contained
rm ../testSignalRInSpring/my-webapp/src/main/resources/static/constants.js





# # Delete the resource group for SignalR
# az group delete --name ExcalifunSignalRGroup --yes --no-wait

# # Delete the resource group for Blob Storage
# az group delete --name ExcalifunStorageGroup --yes --no-wait

# # Delete the resource group fro App Service
# az group delete --name rest-service-2-rg --yes --no-wait

# # Delete the resource group for keyvault
# az group delete --name ExcalifunKeyVaultGroup --yes --no-wait

# # Remove from the azure functions the constants file
# rm ../functions/src/main/java/com/fabrikam/Constants.java

# # Remove from the azure app service the .env file
# rm  ../testSignalRInSpring/my-webapp/src/main/resources/.env 