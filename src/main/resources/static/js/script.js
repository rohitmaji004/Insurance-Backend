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

let selectedPaymentMethod = '';
let purchaseResponse = null;
let currentQuote = null;

// ---------- Event Listeners ----------
getQuoteBtn.addEventListener('click', async () => {
    const fullName = document.getElementById('fullName').value.trim();
    const vehicleNumber = document.getElementById('vehicleNumber').value.trim();

    if (!fullName || !vehicleNumber) {
        quoteError.textContent = 'Please enter both full name and vehicle number.';
        quoteError.classList.remove('hidden');
        return;
    }
    quoteError.classList.add('hidden');

    try {
        const quoteRequest = {
            fullName,
            vehicleNumber,
            durationDays: 365
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

        pageGetQuote.classList.add('hidden');
        pagePaymentMethod.classList.remove('hidden');

    } catch (err) {
        console.error(err);
        alert('Backend not reachable.');
    }
});

nextToCredentialsBtn.addEventListener('click', () => {
    const selectedOption = document.querySelector('input[name="paymentMethod"]:checked');
    const errorMessage = document.getElementById('payment-error');
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
    const cardDetails = getCardDetails();
    if (!cardDetails.cardNumber || !cardDetails.cardHolderName) {
        alert('Please fill all card details.');
        return;
    }
    await makePurchase({ paymentMethod: 'CARD', ...cardDetails });
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

    cardForm.classList.add('hidden');
    upiPaylaterContent.classList.add('hidden');

    if (method === 'CARD') cardForm.classList.remove('hidden');
    else upiPaylaterContent.classList.remove('hidden');

    credentialsHeader.textContent = `Pay via ${method}`;
}

function getCardDetails() {
    return {
        cardNumber: document.getElementById('cardNumber').value.trim(),
        cardHolderName: document.getElementById('cardHolderName').value.trim(),
        expiryMonth: parseInt(document.getElementById('expiryMonth').value, 10),
        expiryYear: parseInt(document.getElementById('expiryYear').value, 10),
        cvv: document.getElementById('cvv').value.trim()
    };
}

async function makePurchase(paymentData) {
    if (!currentQuote) {
        alert('Please get a quote first.');
        return;
    }

    try {
        const purchaseRequest = {
            userId: currentQuote.userId,
            productId: currentQuote.productId,
            vehicleId: currentQuote.vehicleId,
            durationDays: currentQuote.durationDays || 365,
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
