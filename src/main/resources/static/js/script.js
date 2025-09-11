const API_BASE_URL = 'http://localhost:8080/api/policies';
        
// Hardcoded data for demonstration
const MOCK_USER_ID = 1;
const MOCK_PRODUCT_ID = 1;
const MOCK_VEHICLE_ID = 1;
const MOCK_DURATION_DAYS = 365;

document.addEventListener('DOMContentLoaded', () => {
    // Check which page is loaded and run the appropriate script
    if (document.getElementById('page-quote')) {
        setupQuotePage();
    } else if (document.getElementById('page-payment-method')) {
        setupPaymentPage();
    } else if (document.getElementById('page-confirmation')) {
        setupConfirmationPage();
    }
});

function setupQuotePage() {
    const quoteForm = document.getElementById('quote-form');
    const quoteResult = document.getElementById('quote-result');
    const premiumDisplay = document.getElementById('premium-display');
    const proceedToPaymentBtn = document.getElementById('proceed-to-payment');
    const errorMsg = document.getElementById('error-message');

    quoteForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        errorMsg.textContent = '';
        errorMsg.classList.add('hidden');
        
        const payload = {
            userId: MOCK_USER_ID,
            productId: MOCK_PRODUCT_ID,
            vehicleId: MOCK_VEHICLE_ID,
            durationDays: MOCK_DURATION_DAYS
        };

        try {
            const response = await fetch(`${API_BASE_URL}/quote`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to get quote.');
            }

            const quoteData = await response.json();
            
            // Store quote data in session storage and redirect
            sessionStorage.setItem('quoteData', JSON.stringify(quoteData));
            sessionStorage.setItem('paymentPayload', JSON.stringify(payload));
            
            premiumDisplay.textContent = `₹${quoteData.premium.toFixed(2)}`;
            quoteResult.classList.remove('hidden');

        } catch (err) {
            errorMsg.textContent = `Error: ${err.message}`;
            errorMsg.classList.remove('hidden');
        }
    });

    proceedToPaymentBtn.addEventListener('click', () => {
        window.location.href = 'payment.html';
    });
}

function setupPaymentPage() {
    const pages = {
        method: document.getElementById('page-payment-method'),
        credentials: document.getElementById('page-payment-credentials')
    };
    const nextToCredentialsBtn = document.getElementById('next-to-credentials');
    const paymentMethodRadios = document.getElementsByName('paymentMethod');
    const credentialsHeader = document.getElementById('credentials-header');
    const cardForm = document.getElementById('card-form');
    const upiPaylaterContent = document.getElementById('upi-paylater-content');
    const backToMethodBtn = document.getElementById('back-to-method');
    const completePaymentBtn = document.getElementById('complete-payment');
    const errorMsg = document.getElementById('error-message');

    const quoteData = JSON.parse(sessionStorage.getItem('quoteData'));
    const paymentPayload = JSON.parse(sessionStorage.getItem('paymentPayload'));

    if (!quoteData || !paymentPayload) {
        window.location.href = 'index.html';
        return;
    }
    
    // Initial view
    pages.method.classList.remove('hidden');
    pages.credentials.classList.add('hidden');

    nextToCredentialsBtn.addEventListener('click', () => {
        const selectedMethod = document.querySelector('input[name="paymentMethod"]:checked');
        if (!selectedMethod) {
            errorMsg.textContent = "Please select a payment method.";
            errorMsg.classList.remove('hidden');
            return;
        }
        errorMsg.classList.add('hidden');
        
        const method = selectedMethod.value;
        credentialsHeader.textContent = `Pay with ${method}`;

        // Update payload with payment method
        paymentPayload.paymentMethod = method;
        sessionStorage.setItem('paymentPayload', JSON.stringify(paymentPayload));
        
        pages.method.classList.add('hidden');
        pages.credentials.classList.remove('hidden');

        if (method === 'CARD') {
            cardForm.classList.remove('hidden');
            upiPaylaterContent.classList.add('hidden');
        } else {
            cardForm.classList.add('hidden');
            upiPaylaterContent.classList.remove('hidden');
        }
    });

    backToMethodBtn.addEventListener('click', () => {
        pages.method.classList.remove('hidden');
        pages.credentials.classList.add('hidden');
    });
    
    cardForm.addEventListener('submit', (e) => {
        e.preventDefault();
        processPayment('CARD', paymentPayload);
    });

    completePaymentBtn.addEventListener('click', () => {
        processPayment(paymentPayload.paymentMethod, paymentPayload);
    });
}

function setupConfirmationPage() {
    const policyNumberSpan = document.getElementById('policy-number');
    const newPolicyBtn = document.getElementById('new-policy');

    const policyNo = sessionStorage.getItem('policyNo');
    if (policyNo) {
        policyNumberSpan.textContent = policyNo;
    } else {
        policyNumberSpan.textContent = "N/A";
    }

    newPolicyBtn.addEventListener('click', () => {
        sessionStorage.clear();
        window.location.href = 'index.html';
    });
}

async function processPayment(paymentMethod, payload) {
    const errorMsg = document.getElementById('error-message');
    
    // Add card details to the payload if applicable
    if (paymentMethod === 'CARD') {
        payload.cardNumber = document.getElementById('cardNumber').value;
        payload.cardHolderName = document.getElementById('cardHolderName').value;
        payload.expiryMonth = parseInt(document.getElementById('expiryMonth').value);
        payload.expiryYear = parseInt(document.getElementById('expiryYear').value);
        payload.cvv = document.getElementById('cvv').value;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/purchase`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Payment failed. Please try again.');
        }

        // Payment successful, store policy number and redirect
        sessionStorage.setItem('policyNo', data.policyNo);
        window.location.href = 'thankyou.html';

    } catch (err) {
        errorMsg.textContent = `Payment Error: ${err.message}`;
        errorMsg.classList.remove('hidden');
    }
}
