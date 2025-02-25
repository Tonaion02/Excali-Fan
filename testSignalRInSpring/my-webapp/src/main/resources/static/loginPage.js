// Funzione per caricare la pagina di login
function loadLoginPage() {
    const loginHTML = `
        <div class="login-overlay" id="login-overlay">
            <div class="error-message" id="error-message">
                Login fallito. Per favore, riprova.
                <button class="close-error-button" onclick="hideError()">×</button>
            </div>
            <div class="login-form">
            
                <img src="logo-exacalifan.png" alt="Logo" class="login-logo">
                

                <button class="login-button microsoft-login" id="login">
                    <img src="microsoft-logo.png" alt="Microsoft Logo" class="microsoft-logo">
                    Accedi con Microsoft
                </button>
            </div>
        </div>
    `;
    document.getElementById('login-container').innerHTML = loginHTML;
    document.body.classList.add('login-active');
}

// Funzione per chiudere la scheda di login
function closeLogin() {
    document.getElementById('login-overlay').style.display = 'none';
    // document.body.classList.remove('login-active');
}

// Funzione per mostrare il messaggio di errore
function showError() {
    document.getElementById('error-message').style.display = 'block';
}

// Funzione per chiudere il messaggio di errore
function hideError() {
    document.getElementById('error-message').style.display = 'none';
}

// Carica la pagina di login all'avvio
loadLoginPage();