document.addEventListener('DOMContentLoaded', () => {
    const canvas = document.getElementById('drawingCanvas');
    const context = canvas.getContext('2d');

    // Gestione del colore di sfondo al clic
    const pencilContainer = document.getElementById('pencil-container');
    const eraserContainer = document.getElementById('eraser-container');
    // const colorDropdown = document.getElementById('color-dropdown');

    // const colorOptions = [
    //     { color: '#ffffff', element: document.createElement('div') },
    //     { color: '#ff69b4', element: document.createElement('div') },
    //     { color: '#006400', element: document.createElement('div') },
    //     { color: '#00008b', element: document.createElement('div') }
    // ];

    // colorOptions.forEach(option => {
    //     const wrapper = document.createElement('div');
    //     wrapper.classList.add('color-wrapper');
    //     option.element.classList.add('color-option');
    //     option.element.style.backgroundColor = option.color;
    //     option.element.setAttribute('data-color', option.color);
    //     wrapper.appendChild(option.element);
    //     colorDropdown.appendChild(wrapper);

    //     option.element.addEventListener('click', () => {
    //         context.strokeStyle = option.color;
    //         document.querySelectorAll('.color-wrapper').forEach(wrap => wrap.classList.remove('selected'));
    //         wrapper.classList.add('selected');
    //         colorDropdown.style.display = 'none';
    //     });
    // });

    // Imposta il colore bianco come colore selezionato di base
    // document.querySelector('.color-option[data-color="#ffffff"]').parentElement.classList.add('selected');
    context.strokeStyle = '#ffffff';

    pencilContainer.addEventListener('click', () => {
        pencilContainer.style.backgroundColor = '#403e6a'; // Colore viola più scuro
        eraserContainer.style.removeProperty('background-color');
        colorDropdown.style.display = colorDropdown.style.display === 'flex' ? 'none' : 'flex';
    });

    eraserContainer.addEventListener('click', () => {
        eraserContainer.style.backgroundColor = '#403e6a'; // Colore viola più scuro
        pencilContainer.style.removeProperty('background-color');
        // colorDropdown.style.display = 'none';
    });

    // Nascondi la tendina dei colori all'inizio
    // colorDropdown.style.display = 'none';

    // Gestione del menu
    const menuButton = document.getElementById('menu-button');
    const menu = document.getElementById('menu');
    const exportImageButton = document.getElementById('export-image');
    const newBoardButton = document.getElementById("newBoardButton");
    const lodeBoardContainer = document.querySelector('.lode-board-container');
    const lodeBoardDropdown = document.getElementById('lode-board-dropdown');

    menuButton.addEventListener('mousedown', (event) => {
        event.stopPropagation(); // Impedisce la propagazione del click al documento
        const isMenuVisible = menu.style.display === 'block';
        menu.style.display = isMenuVisible ? 'none' : 'block';
    });

    document.addEventListener('mousedown', (event) => {
        if (menu.style.display === 'block' && !menu.contains(event.target) && event.target !== menuButton) {
            menu.style.display = 'none';
        }
    });



    // Gestione del box di condivisione
    const shareButton = document.querySelector('.share-button');
    const shareBox = document.getElementById('share-box');

    shareButton.addEventListener('click', () => {
        shareBox.style.display = shareBox.style.display === 'block' ? 'none' : 'block';
    });

    // Mostra la lista di nomi quando il cursore è sul bottone "Lode Board" o sulla lista stessa
    lodeBoardContainer.addEventListener('mouseenter', () => {
        lodeBoardDropdown.style.display = 'block';
    });

    lodeBoardContainer.addEventListener('mouseleave', () => {
        setTimeout(() => {
            if (!lodeBoardDropdown.matches(':hover') && !lodeBoardContainer.matches(':hover')) {
                lodeBoardDropdown.style.display = 'none';
            }
        }, 125);
    });

    lodeBoardDropdown.addEventListener('mouseenter', () => {
        lodeBoardDropdown.style.display = 'block';
    });

    lodeBoardDropdown.addEventListener('mouseleave', () => {
        setTimeout(() => {
            if (!lodeBoardDropdown.matches(':hover') && !lodeBoardContainer.matches(':hover')) {
                lodeBoardDropdown.style.display = 'none';
            }
        }, 125);
    });


   lodeBoardDropdown.style.display = 'none';


    const saveModal = document.getElementById('save-modal');
    const saveButton = document.getElementById('export-image');
    const closeButton = document.querySelector('.close-button');

    saveButton.addEventListener('click', () => {
        saveModal.style.display = 'block';
    });

    closeButton.addEventListener('click', () => {
        saveModal.style.display = 'none';
    });

    window.addEventListener('click', (event) => {
        if (event.target === saveModal) {
            saveModal.style.display = 'none';
        }
    });


    const loadingOverlay = document.getElementById("loadingOverlay");
    const joinGroupButton = document.getElementById("add-group-button");
});