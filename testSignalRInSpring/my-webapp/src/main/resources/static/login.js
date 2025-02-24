const msalConfig = {
  auth: {
    clientId: "b1453203-8719-4a2a-8cc6-96bf883a7e65",
    authority: "https://login.microsoftonline.com/common",
    redirectUri: "https://rest-service-1735827345127.azurewebsites.net/",
    // redirectUri: "https://rest-service-1735827345127.azurewebsites.net/api/testEntraId",
  },
};

let msalInstance;

import("https://alcdn.msauth.net/browser/2.38.2/js/msal-browser.min.js").then(() => {
  console.log("MSAL loaded");
  msalInstance = new msal.PublicClientApplication(msalConfig);
});

// T: WARNING check if it is necessary to use async to keep working this functions
async function login() {

  // T: TODO add a lock to the page when the popup is generated

  console.log("Starting login");

  const loginRequest = {
    scopes: ["User.Read"],
    prompt: "select_account",
  };

  try {
    if (!msalInstance) {
      console.error("MSAL instance not initialized");
      return;
    }

    const loginResponse = await msalInstance.loginPopup(loginRequest);
    console.log("Access Token:", loginResponse.accessToken);

  } catch (error) {
    console.error("Login failed:", error);
  }

  // T: TODO at the end of this routine unlock the page
}