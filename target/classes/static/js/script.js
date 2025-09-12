const API_BASE_URL = 'http://localhost:8080/api/policies';

// ---------- Elements ----------
const pageGetQuote = document.getElementById('page-get-quote');
const pagePaymentMethod = document.getElementById('page-payment-method');
const pagePaymentCredentials = document.getElementById('page-payment-credentials');
const nextToCredentialsBtn = document.getElementById('next-to-credentials');
const backToMethodBtn = document.getElementById('back-to-method');
const quoteError = document.getElementById('quote-error');
const getQuoteBtn = document.getElementById('get-quote');

const cardForm = document.getElementById('card-form');
const upiPaylaterContent = document.getElementById('upi-paylater-content');

const credentialsHeader = document.getElementById('credentials-header');
const completePaymentBtn = document.getElementById('complete-payment');

const pageConfirmation = document.getElementById('page-confirmation');
const policyNumberSpan = document.getElementById('policy-number');
const newPolicyBtn = document.getElementById('new-policy');

// ---------- State ----------
let selectedPaymentMethod = '';
let purchaseResponse = null;
let currentQuote = null; // Store the quote response here

// ---------- Event Listeners ----------
getQuoteBtn.addEventListener('click', async () => {
    const fullName = document.getElementById('fullName').value;
    const vehicleNumber = document.getElementById('vehicleNumber').value;

    if (!fullName || !vehicleNumber) {
        quoteError.textContent = 'Please enter both full name and vehicle number.';
        quoteError.classList.remove('hidden');
        return;
    }
    quoteError.classList.add('hidden');

    try {
        const quoteRequest = {
            fullName: fullName,
            vehicleNumber: vehicleNumber,
            durationDays: 365, // Example: 1 year policy
        };

        const response = await fetch(`${API_BASE_URL}/quote`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(quoteRequest)
        });

        if (!response.ok) {
            const msg = await response.text();
            alert(`Quote failed: ${msg}`);
            return;
        }

        currentQuote = await response.json();
        
        // Show quote details and proceed to payment method selection
        document.getElementById('page-get-quote').classList.add('hidden');
        document.getElementById('page-payment-method').classList.remove('hidden');

    } catch (err) {
        console.error(err);
        alert('Backend not reachable.');
    }
});

nextToCredentialsBtn.addEventListener('click', () => {
    const selectedOption = document.querySelector('input[name="paymentMethod"]:checked');
    if (!selectedOption) {
        errorMessage.textContent = 'Please select a payment method.';
        errorMessage.classList.remove('hidden');
        return;
    }
    errorMessage.classList.add('hidden');
    selectedPaymentMethod = selectedOption.value;
    showPaymentCredentials(selectedPaymentMethod);
});

backToMethodBtn.addEventListener('click', () => {
    pagePaymentCredentials.classList.add('hidden');
    pagePaymentMethod.classList.remove('hidden');
});

cardForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    await makePurchase({ paymentMethod: 'CARD', card: getCardDetails() });
});

completePaymentBtn.addEventListener('click', async () => {
    if (selectedPaymentMethod === 'UPI' || selectedPaymentMethod === 'PAYLATER') {
        await makePurchase({ paymentMethod: selectedPaymentMethod });
    }
});

newPolicyBtn.addEventListener('click', () => {
    pageConfirmation.classList.add('hidden');
    pageGetQuote.classList.remove('hidden');
    cardForm.reset();
    currentQuote = null;
});

// ---------- Functions ----------
function showPaymentCredentials(method) {
    pagePaymentMethod.classList.add('hidden');
    pagePaymentCredentials.classList.remove('hidden');

    // Hide all first
    cardForm.classList.add('hidden');
    upiPaylaterContent.classList.add('hidden');

    if (method === 'CARD') cardForm.classList.remove('hidden');
    else upiPaylaterContent.classList.remove('hidden');

    credentialsHeader.textContent = `Pay via ${method}`;
}

function getCardDetails() {
    return {
        cardNumber: document.getElementById('cardNumber').value,
        cardHolderName: document.getElementById('cardHolderName').value,
        expiryMonth: document.getElementById('expiryMonth').value,
        expiryYear: document.getElementById('expiryYear').value,
        cvv: document.getElementById('cvv').value
    };
}

async function makePurchase(paymentData) {
    if (!currentQuote) {
        alert('Please get a quote first.');
        return;
    }
    
    try {
        const purchaseRequest = {
            userId: currentQuote.getUserId(), // <-- Using IDs from the quote response
            productId: currentQuote.getProductId(),
            vehicleId: currentQuote.getVehicleId(),
            durationDays: currentQuote.getDurationDays(),
            ...paymentData
        };

        const response = await fetch(`${API_BASE_URL}/purchase`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(purchaseRequest)
        });

        if (!response.ok) {
            const msg = await response.text();
            alert(`Payment failed: ${msg}`);
            return;
        }

        purchaseResponse = await response.json();
        showConfirmation(purchaseResponse);
    } catch (err) {
        console.error(err);
        alert('Backend not reachable.');
    }
}

function showConfirmation(data) {
    pagePaymentCredentials.classList.add('hidden');
    pagePaymentMethod.classList.add('hidden');
    pageConfirmation.classList.remove('hidden');

    policyNumberSpan.textContent = data.policyNo || 'N/A';
}