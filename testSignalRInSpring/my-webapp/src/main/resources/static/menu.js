document.addEventListener('DOMContentLoaded', () => {
    const canvas = document.getElementById('drawingCanvas');
    const context = canvas.getContext('2d');

    // Gestione del colore di sfondo al clic
    const pencilContainer = document.getElementById('pencil-container');
    const eraserContainer = document.getElementById('eraser-container');
    const colorDropdown = document.getElementById('color-dropdown');

    const colorOptions = [
        { color: '#ffffff', element: document.createElement('div') },
        { color: '#ff69b4', element: document.createElement('div') },
        { color: '#006400', element: document.createElement('div') },
        { color: '#00008b', element: document.createElement('div') }
    ];

    colorOptions.forEach(option => {
        const wrapper = document.createElement('div');
        wrapper.classList.add('color-wrapper');
        option.element.classList.add('color-option');
        option.element.style.backgroundColor = option.color;
        option.element.setAttribute('data-color', option.color);
        wrapper.appendChild(option.element);
        colorDropdown.appendChild(wrapper);

        option.element.addEventListener('click', () => {
            context.strokeStyle = option.color;
            document.querySelectorAll('.color-wrapper').forEach(wrap => wrap.classList.remove('selected'));
            wrapper.classList.add('selected');
            colorDropdown.style.display = 'none';
        });
    });

    // Imposta il colore bianco come colore selezionato di base
    document.querySelector('.color-option[data-color="#ffffff"]').parentElement.classList.add('selected');
    context.strokeStyle = '#ffffff';

    pencilContainer.addEventListener('click', () => {
        pencilContainer.style.backgroundColor = '#403e6a'; // Colore viola più scuro
        eraserContainer.style.removeProperty('background-color');
        colorDropdown.style.display = colorDropdown.style.display === 'flex' ? 'none' : 'flex';
    });

    eraserContainer.addEventListener('click', () => {
        eraserContainer.style.backgroundColor = '#403e6a'; // Colore viola più scuro
        pencilContainer.style.removeProperty('background-color');
        colorDropdown.style.display = 'none';
    });

    // Nascondi la tendina dei colori all'inizio
    colorDropdown.style.display = 'none';

    // Gestione del menu
    const menuButton = document.getElementById('menu-button');
    const menu = document.getElementById('menu');
    const exportImageButton = document.getElementById('export-image');
    const saveButton = document.getElementById('save');

    menuButton.addEventListener('click', () => {
        menu.style.display = menu.style.display === 'block' ? 'none' : 'block';
    });

    exportImageButton.addEventListener('click', () => {
        const link = document.createElement('a');
        link.download = 'disegno.png';
        link.href = canvas.toDataURL();
        link.click();
    });

    saveButton.addEventListener('click', () => {
        // Aggiungi qui il codice per salvare il disegno
        alert('Funzione di salvataggio non implementata');
    });

    // Gestione delle dimensioni del canvas
    // canvas.width = window.innerWidth;
    // canvas.height = window.innerHeight;
    // window.addEventListener('resize', () => {
    //     canvas.width = window.innerWidth;
    //     canvas.height = window.innerHeight;
    // });

    // Gestione del box di condivisione
    const shareButton = document.querySelector('.share-button');
    const shareBox = document.getElementById('share-box');
    // const addGroupButton = document.getElementById('add-group-button');
    // const currentGroupLabel = document.getElementById('current-group-label');

    shareButton.addEventListener('click', () => {
        shareBox.style.display = shareBox.style.display === 'block' ? 'none' : 'block';
    });

    // addGroupButton.addEventListener('click', () => {
    //     addGroup();
    // });

    // function addGroup() {
    //     const groupName = document.getElementById('group-name').value;
    //     currentGroupLabel.textContent = `Current Group: ${groupName}`;
        
    //     // Add your logic here
    // }
});