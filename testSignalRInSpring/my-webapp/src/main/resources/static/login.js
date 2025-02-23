const msalConfig = {
  auth: {
    clientId: "b1453203-8719-4a2a-8cc6-96bf883a7e65",
    authority: "https://login.microsoftonline.com/d5364e8d-9d78-49f0-9636-be777bbe9507",
    //redirectUri: "https://rest-service-1735827345127.azurewebsites.net/api/testEntraId",
    redirectUri: "https://rest-service-1735827345127.azurewebsites.net/",
  },
};

console.log(msal);

let msalInstance = null
import("https://alcdn.msauth.net/browser/2.38.2/js/msal-browser.min.js").then(() => {
    console.log("hey");
    msalInstance = new msal.PublicClientApplication(msalConfig)
});
 


function login() {
    console.log("hey2");

  const loginRequest = {
    scopes: ["User.Read"],
    prompt: 'select_account',
    forceRefresh: true,
  };

  try {
    const loginResponse = msalInstance.loginPopup(loginRequest);
    loginResponse.then((response) => {console.log("Access Token:", response.accessToken)});
  } catch (error) {
    console.error(error);
  }
}
