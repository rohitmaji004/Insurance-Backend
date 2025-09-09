const API_URL = 'http://localhost:8080/api';

// --- LOGIN & REGISTER ---
document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
    // Add similar event listener for registration form if you have one
});

document.addEventListener('DOMContentLoaded', () => {
    // ... your existing loginForm event listener ...

    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }
});

async function handleRegister(event) {
    event.preventDefault(); // Stop the browser from submitting the form directly
    const messageDiv = document.getElementById('message');

    // Create a JSON object from the form fields
    const user = {
        firstName: document.getElementById('firstName').value,
        lastName: document.getElementById('lastName').value,
        email: document.getElementById('email').value,
        password: document.getElementById('password').value,
        phoneNumber: document.getElementById('phoneNumber').value,
        address: document.getElementById('address').value
    };

    try {
        const response = await fetch(`${API_URL}/users/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(user)
        });

        const data = await response.json();

        if (!response.ok) {
            // The API returns an error message in the body
            throw new Error(data.message || 'Registration failed.');
        }

        messageDiv.className = 'successMessage';
        messageDiv.textContent = 'Registration successful! Please login.';
        document.getElementById('registerForm').reset(); // Clear the form

    } catch (error) {
        messageDiv.className = 'errorMessage';
        messageDiv.textContent = error.message;
    }
}

async function handleLogin(event) {
    event.preventDefault();
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const errorMessageDiv = document.getElementById('errorMessage');

    try {
        const response = await fetch(`${API_URL}/users/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        if (!response.ok) {
            throw new Error('Login failed. Please check your credentials.');
        }

        const data = await response.json();
        localStorage.setItem('jwtToken', data.token); // Store the token
        //window.location.href = '/dashboard'; // Redirect to dashboard

    } catch (error) {
        errorMessageDiv.textContent = error.message;
    }
}

// --- DASHBOARD ---
async function loadDashboard() {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        window.location.href = '/login'; // If no token, redirect to login
        return;
    }

    try {
        const response = await fetch(`${API_URL}/users/profile`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.status === 401 || response.status === 403) {
             // Token is invalid or expired
            logout();
        }
        if (!response.ok) {
            throw new Error('Failed to fetch user data.');
        }

        const user = await response.json();
        displayUserInfo(user);
        displayVehicles(user.vehicles);

    } catch (error) {
        document.getElementById('dashboardError').textContent = error.message;
    }
}

function displayUserInfo(user) {
    const userInfoDiv = document.getElementById('userInfo');
    userInfoDiv.innerHTML = `
        <p><strong>Name:</strong> ${user.firstName} ${user.lastName}</p>
        <p><strong>Email:</strong> ${user.email}</p>
    `;
}

function displayVehicles(vehicles) {
    const tableBody = document.getElementById('vehiclesTbody');
    tableBody.innerHTML = ''; // Clear existing rows
    if (!vehicles || vehicles.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="4">No vehicles found.</td></tr>';
        return;
    }

    vehicles.forEach(vehicle => {
        const row = `<tr>
            <td>${vehicle.id}</td>
            <td>${vehicle.registrationNumber}</td>
            <td>${vehicle.vehicleType}</td>
            <td>${vehicle.ownerName}</td>
        </tr>`;
        tableBody.innerHTML += row;
    });
}

function logout() {
    localStorage.removeItem('jwtToken');
    window.location.href = '/login';
}