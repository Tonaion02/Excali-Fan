const msalConfig = {
  auth: {
    clientId: "b1453203-8719-4a2a-8cc6-96bf883a7e65",
    authority: "https://login.microsoftonline.com/common",
    redirectUri: "https://rest-service-1735827345127.azurewebsites.net/",
  },
  cache: {
    cacheLocation: "sessionStorage",
    storeAuthStateInCookie: false,
  },
  system: {
    loggerOptions: {
      loggerCallback: (level, message, containsPii) => {
        if (containsPii) return;
        switch (level) {
          case msal.LogLevel.Error:
            console.error(message);
            break;
          case msal.LogLevel.Info:
            console.info(message);
            break;
          case msal.LogLevel.Verbose:
            console.debug(message);
            break;
          case msal.LogLevel.Warning:
            console.warn(message);
            break;
        }
      },
    },
  },
};

let msalInstance;

import("https://alcdn.msauth.net/browser/2.38.2/js/msal-browser.min.js").then(() => {
  console.log("MSAL loaded");
  msalInstance = new msal.PublicClientApplication(msalConfig);
});

const loginRequest = {
  scopes: ["openid", "profile", "email", "api://b1453203-8719-4a2a-8cc6-96bf883a7e65/loginScope"],
  prompt: "select_account",
};

const tokenRequest = {
  scopes: ["openid", "profile", "email", "api://b1453203-8719-4a2a-8cc6-96bf883a7e65/loginScope"],
  prompt: "select_account",
};

async function login() {
  try {
    if (!msalInstance) {
      console.error("MSAL instance not initialized");
      return;
    }

    console.log("Starting login");
    const loginResponse = await msalInstance.loginPopup(loginRequest);
    console.log("Login successful");

    // Set active account
    msalInstance.setActiveAccount(loginResponse.account);

    let tokenResponse;
    try {
      // Try acquiring token silently
      tokenResponse = await msalInstance.acquireTokenSilent({
        ...tokenRequest,
        account: loginResponse.account,
      });
    } catch (silentError) {
      console.warn("Silent token acquisition failed, acquiring token via popup...", silentError);
      tokenResponse = await msalInstance.acquireTokenPopup(tokenRequest);
    }

    console.log("Access Token:", tokenResponse.accessToken);



    // T: verify if the token is valid (START)
    axios.post("https://rest-service-1735827345127.azurewebsites.net/publicApi/verifyLoginToken", {}, {
      headers: {
        "Authorization": tokenResponse.accessToken,
        "Content-Type": "application/json"
      }
    })
      .then(response => {
        console.log("Status verification token:", response.status);
        // T: TODO remove overlay of login and call setup function

      })
      .catch(error => {
        console.error("Error during verification of token:", error);
      });
    // T: verify if the token is valid (END)
  } catch (error) {
    console.error("Login or token retrieval failed:", error);
    // T: TODO add error message for unsucess login
    
  }
}