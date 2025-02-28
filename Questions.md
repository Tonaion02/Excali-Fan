# Questions
- 1: mostriamogli fin dove siamo arrivati, nel senso, c'abbiamo lo strumento di disegno, quello per cancellare... funziona in questa determinata maniera e ok...
- 2: Noi abbiamo pensato di realizzare il login attraverso microsoft entra id, non siamo riusciti a capire se abbiamo fatto tutto nella maniera corretta, i nostri dubbi sono:
    - Ti sembra corretto questo processo di login che utilizza un popup per una single page application?
    - Noi avevamo pensato di generare il token attraverso il login dal Client e conservarlo in locale tramite il session storage. A questo punto, ogni richiesta effettuata al server dovrà andare a verificare se il token passato è valido. Questo workflow ti sembra corretto? Non sarebbe meglio utilizzare almeno per le richieste al server una sorta di meccanismo più simile alla sessione?
- 3: Secondo te abbiamo utilizzato Azure Key Vault in maniera sensata? Noi lo abbiamo utilizzato per conservare la key di Azure SignalR che il server si va a recuperare solamente quando viene startato.
- 4: Architettura:
    - Secondo te è sensato l'utilizzo che stiamo facendo dei vari servizi?
- 5: fun fact mozilla e mac