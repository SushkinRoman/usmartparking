// === 1. ОБЩИЕ ФУНКЦИИ ===
async function apiCall(url, method = 'GET', data = null) {
    const options = {
        method: method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (data) {
        options.body = JSON.stringify(data);
    }
    const response = await fetch(url, options);
    return await response.json();
}

// === 2. АВТОРИЗАЦИЯ И РЕГИСТРАЦИЯ ===
async function handleLogin(e) {
    e.preventDefault(); // ВАЖНО: Останавливаем стандартную перезагрузку страницы!

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    try {
        const result = await apiCall('/api/auth/login', 'POST', { email, password });

        if (result.success) {
            // Сохраняем данные пользователя (опционально, но полезно)
            localStorage.setItem('userRole', result.role);
            localStorage.setItem('userName', result.nickname);

            // Перенаправление в зависимости от роли
            if (result.role === 'АДМИНИСТРАТОР') {
                window.location.href = 'stats.html';
            } else {
                window.location.href = 'map.html';
            }
        } else {
            alert(result.message);
        }
    } catch (error) {
        console.error("Ошибка входа:", error);
        alert("Ошибка соединения с сервером. Убедитесь, что Spring Boot запущен.");
    }
}

async function handleRegister(e) {
    e.preventDefault(); // Останавливаем перезагрузку

    const data = {
        nickname: document.getElementById('nickname').value,
        email: document.getElementById('email').value,
        password: document.getElementById('password').value,
        role: document.getElementById('role').value
    };

    try {
        const result = await apiCall('/api/auth/register', 'POST', data);
        if (result.success) {
            alert('Регистрация успешна! Теперь войдите.');
            window.location.href = 'login.html';
        } else {
            alert(result.message);
        }
    } catch (error) {
        console.error("Ошибка регистрации:", error);
        alert("Ошибка соединения с сервером.");
    }
}

// === 3. КАРТА ПАРКОВКИ ===
async function loadMap() {
    const grid = document.getElementById('parkingGrid');
    if (!grid) return; // Если мы не на странице карты, выходим

    try {
        const data = await apiCall('/api/parking/map');
        grid.innerHTML = ''; // Очищаем сетку перед перерисовкой

        const rows = ['A', 'B', 'C', 'D', 'E', 'F'];
        rows.forEach(row => {
            for (let col = 1; col <= 6; col++) {
                const id = `${row}-${col.toString().padStart(2, '0')}`;
                const space = data[id];

                const div = document.createElement('div');
                div.className = `space ${space.status}`;
                div.innerText = `${id}\n${space.status}`;
                div.style.whiteSpace = 'pre-line';
                grid.appendChild(div);
            }
        });

        // Добавляем блок въезда
        const entrance = document.createElement('div');
        entrance.className = 'entrance';
        entrance.innerText = '⬇ ВЪЕЗД / ВЫЕЗД ⬇';
        grid.appendChild(entrance);
    } catch (error) {
        console.error("Ошибка загрузки карты:", error);
    }
}

// === 4. СТАТИСТИКА И ГРАФИК ===
async function loadStatsChart() {
    const canvas = document.getElementById('occupancyChart');
    if (!canvas) return; // Если мы не на странице статистики, выходим

    try {
        // Запрашиваем корректный эндпоинт, который есть в ParkingController
        const historyData = await apiCall('/api/parking/history');
        if (!historyData || historyData.length === 0) return;

        const ctx = canvas.getContext('2d');

        // Уничтожаем старый график, чтобы они не накладывались друг на друга
        if (window.myChart) {
            window.myChart.destroy();
        }

        const labels = historyData.map(item => item.timestamp);
        const freeData = historyData.map(item => item.freeCount);
        const occupiedData = historyData.map(item => item.occupiedCount);
        const reservedData = historyData.map(item => item.reservedCount);

        window.myChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Свободно',
                        data: freeData,
                        borderColor: '#28a745',
                        backgroundColor: 'rgba(40, 167, 69, 0.2)',
                        tension: 0.3,
                        fill: true
                    },
                    {
                        label: 'Занято',
                        data: occupiedData,
                        borderColor: '#dc3545',
                        backgroundColor: 'rgba(220, 53, 69, 0.2)',
                        tension: 0.3,
                        fill: true
                    },
                    {
                        label: 'Забронировано',
                        data: reservedData,
                        borderColor: '#ffc107',
                        backgroundColor: 'rgba(255, 193, 7, 0.2)',
                        tension: 0.3,
                        fill: true
                    }
                ]
            },
            options: {
                responsive: true,
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 36, // Всего мест 6x6 = 36
                        title: { display: true, text: 'Количество мест' }
                    },
                    x: {
                        title: { display: true, text: 'Время' }
                    }
                }
            }
        });

        // Обновляем цифры в верхних плашках последними данными
        const latest = historyData[historyData.length - 1];
        document.getElementById('freeCount').innerText = latest.freeCount;
        document.getElementById('occupiedCount').innerText = latest.occupiedCount;
        document.getElementById('reservedCount').innerText = latest.reservedCount;

    } catch (error) {
        console.error("Ошибка загрузки статистики:", error);
    }
}

// === 5. ИНИЦИАЛИЗАЦИЯ ПРИ ЗАГРУЗКЕ СТРАНИЦЫ ===
document.addEventListener('DOMContentLoaded', () => {
    // 1. Привязываем обработчики к формам (если они есть на текущей странице)
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }

    // 2. Если мы на странице карты, загружаем её и обновляем каждые 2 сек
    if (document.getElementById('parkingGrid')) {
        loadMap();
        setInterval(loadMap, 2000);
    }

    // 3. Если мы на странице статистики, загружаем график и обновляем каждые 3 сек
    if (document.getElementById('occupancyChart')) {
        loadStatsChart();
        setInterval(loadStatsChart, 3000);
    }
});