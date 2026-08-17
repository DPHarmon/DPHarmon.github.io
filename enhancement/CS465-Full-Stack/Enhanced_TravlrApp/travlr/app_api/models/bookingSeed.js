/**
 *  Booking Seed
 * 
 *  Analytics requires transactional records. bookingSeed generates
 *  synthetic users and bookings against the trips currently present
 *  in the database. 
 * 
 *  Date: 7/30/2026
 *  Author: Dylan Harmon
 */

// DB connection and required Schemas
const Mongoose  = require('./db');
const Trip      = require('./travlr');
const user      = require('./user');
const Booking   = require('./bookings');
const User = require('./user');

/**
 *  Configuriation
 * 
 *  Tunabale constants for generation. NUM_USERS, MONTHS_OF_HISTORY,
 *  BASE_BOOKINGS_PER_MONTH, CANCELLATION_RATE, COMPLETION_AFTER_DAYS,
 *  PRICE_SENSITIVITY, REFERENCE_PRICE
 */
const NUM_USERS                 = 50;
const MONTHS_OF_HISTORY         = 18;
const BASE_BOOKINGS_PER_MONTH   = 14;
const CANCELLATION_RATE         = 0.12;
const COMPLETION_AFTER_DAYS     = 60;
const PRICE_SENSITIVITY         = 1.4;
const REFERENCE_PRICE           = 1200.0;
/**
 *  Users are tagged by email domain. Allows synthetic data to be cleared
 *  upon re-run of seed data.
 */
const USER_EMAIL_DOMAIN = 'travlrseed.test';

/**
 *  Seasonal Weights, indexed by month
 * 
 *  Bookings are not distributed evenly. Travel ramps up during the summer
 *  months. This adds a more realistic layer to the visual data we will see
 *  later.
 */
const SEASONAL_WEIGHTS = [
    0.6,    // January
    0.7,    // February
    0.9,    // March
    0.7,    // April
    1.2,    // May
    1.5,    // June
    1.6,    // July
    1.5,    // August
    0.9,    // September
    0.8,    // October
    0.9,    // November
    1.1     // December
];

/**
 *  Names for user generation
 */
const FIRST_NAMES = [
    'Ava', 'Leanord', 'Maya', 'Alex', 'Steficia', 'Nick', 'Sofia', 'Kate',
    'Nadia', 'Maria', 'Mateo', 'Isabella', 'Ezra', 'Austin', 'Max', 'Millie',
    'Jonah', 'Tom', 'Becca', 'Robert', 'Emily', 'Emma', 'Pedro', 'Iris',
    'Aurora', 'Carla', 'Elizabeth', 'Heather', 'Veronica', 'Eloisa'
];

const LAST_NAMES = [
    'Vazquez', 'Piqueras', 'Haeri', 'Barros', 'Delgado', 'Reyes', 'Vance',
    'Novotny', 'Cootes', 'Ibarra', 'Sterling', 'Padilla', 'Shingler',
    'Chandler', 'Esparaza', 'Axe', 'Verdin', 'Ashford', 'Whitfield', 'Sorensen',
    'Petterson', 'Scott', 'Smith', 'Cabral', 'Kennedy', 'Ramirez', 'Garcia',
    'Volkman', 'Lancaster', 'Hernandez'
];

// intarrayRandomizer - returns random integer between min and max
const intRandomizer = (min, max) => {
    return Math.floor(Math.random() * (max - min + 1)) + min;
};

// Randomizer - generates random element from array
const arrayRandomizer = (array) => {
    return array[Math.floor(Math.random() * array.length)];
};

/**
 *  Booking Groupd Size Wights. Solo and Couple bookings
 *  are more frequent than big group bookings.
 */
const randomTravelers = () => {
    const roll = Math.random();
    if (roll < 0.18) return 1; // 0.00 - 0.18: 18%
    if (roll < 0.62) return 2; // 0.18 - 0.62: 44%
    if (roll < 0.82) return 3; // 0.62 - 0.82: 20%
    if (roll < 0.94) return 4; // 0.82 - 0.94: 12%
    return intRandomizer(5, 6);    // 0.94 - 1.00: 6%
};

/**
 *  Added Trips for a better visual representation in the dashboard.
 *  Each trip receives a weight derived from its price.
 * 
 * Returns cumulative thresholds so a single random draw can selet a trip
 * in a single pass. Same as randomTravlers.
 */
const buildTripWeights = (trips) => {
    const weights = trips.map((trip) => {
        const price = parseFloat(trip.perPerson);

        // cheaper price : greater weight
        const base = Math.pow(REFERENCE_PRICE / price, PRICE_SENSITIVITY);

        // fixed per-trip variation.
        const tripVariation = 0.6 + (Math.random() * 0.9);

        return base * tripVariation;

    });

    const total = weights.reduce((sum, w) => sum + w, 0);

    // Convert to thresholds
    const thresholds = [];
    let running = 0;
    for (const weight of weights) {
        running += weight;
        thresholds.push(running / total);
    }

    return thresholds;
};

/**
 *  Select a trip using the cumalitve threshold table.
 */
const weightedTrip = (trips, thresholds) => {
    const roll = Math.random();
    for (let i = 0; i < thresholds.length; i++) {
        if (roll < thresholds[i]) {
            return trips[i];
        }
    }

    return trips[trips.length -1];
};

/**
 *  Synthetic User Generator
 * 
 *  Each document is instantiated through the model so that setPassword()
 *  is available. That method generated the alt and PBKDF2 hash exactly as
 *  the registration endpoint does, which keeps the seeded uses structurally
 *  identical to the real users.
 */
const buildUsers = () => {
    const users = [];
    const usedEmails = new Set();

    for (let i = 0; i < NUM_USERS; i++) {
        const first = arrayRandomizer(FIRST_NAMES);
        const last = arrayRandomizer(LAST_NAMES);

        // email field carries a unique index. Collisions must be
        // resolved before insert rather than caught as a duplicate key error
        let email = `${first}.${last}@${USER_EMAIL_DOMAIN}`.toLowerCase();

        let suffix = 1;

        while (usedEmails.has(email)) {
            email = `${first}.${last}${suffix}@${USER_EMAIL_DOMAIN}`
            .toLowerCase();
            suffix++;
        }
        usedEmails.add(email);

        const user = new User({
            name: `${first} ${last}`,
            email: email
        });
        user.setPassword('SeedPassword123!');
        users.push(user);
    }

    return users;
}

/**
 *  Determin Booking Status
 */
const resolveStatus = (bookingDate, now) => {
    if(Math.random() < CANCELLATION_RATE) {
        return 'cancelled';
    }

    const daysElapsed = (now - bookingDate) / (1000 * 60 * 60 * 24);
    return daysElapsed > COMPLETION_AFTER_DAYS ? 'completed' : 'confirmed';
};

/**
 *  Synthetic Booking Generator
 * 
 *  Starts from most current month and walks backwards. The count for each month
 *  is the base volume scaled by that month's seasonal weight and by a growth
 *  factor that rises towards the present. For visualizing trends.
 */
const buildBookings = (trips, users) => {
    const bookings = [];
    const now      = new Date();
    const tripThresholds = buildTripWeights(trips);
    
    for (let monthsAgo = MONTHS_OF_HISTORY - 1; monthsAgo >= 0; monthsAgo--) {
        const monthStart = new Date(
            now.getFullYear(),
            now.getMonth() - monthsAgo,
            1
        );

        const seasonal = SEASONAL_WEIGHTS[monthStart.getMonth()];
        // Growth increase
        const growth = 0.75 + (0.25 * ((MONTHS_OF_HISTORY - monthsAgo) /MONTHS_OF_HISTORY));
        // Fluctuate growth - randomizer for growth
        const jitter = 0.85 + (Math.random() *0.3);

        const count = Math.max(
            1,
            Math.round(BASE_BOOKINGS_PER_MONTH * seasonal * growth * jitter)
        );
        // Number of days in `this` month, Accounting for leap years.
        // Day 0 of the following month resolves to the last day of this month.
        const daysInMonth = new Date(
            monthStart.getFullYear(),
            monthStart.getMonth() + 1,
            0
        ).getDate();

        for (let i = 0; i < count; i++) {
            const trip = weightedTrip(trips, tripThresholds);
            const user = arrayRandomizer(users);
            const numTravelers = randomTravelers();

            const bookingDate = new Date(
                monthStart.getFullYear(),
                monthStart.getMonth(),
                intRandomizer(1, daysInMonth),
                intRandomizer(8,21),
                intRandomizer(0, 59)
            );

            if (bookingDate > now) {
                continue;
            }

            /**
             *  perPerson is stored in the Trip schema as a string. Needs to be
             *  a number for data visualization.
             */
            const perPerson = parseFloat(trip.perPerson);
            const totalPrice = Math.round(perPerson * numTravelers * 100) / 100;

            bookings.push({
                trip: trip._id,
                user: user._id,
                bookingDate: bookingDate,
                numTravelers: numTravelers,
                totalPrice: totalPrice,
                status: resolveStatus(bookingDate, now)
            });
        }
    }

    return bookings;
};


/**
 *  Seed Routing
 * 
 *  Existing seed records are removed before insert. 
 */
const seedBookings = async () => {
    const trips = await Trip.find({});

    if (trips.length === 0) {
        throw new error(
            'No trips found. Run the trip seed first: npm run seed'
        );
    }

    // Clear prior synthetic bookings.
    await Booking.deleteMany({});
    await User.deleteMany({email: {$regex: `@${USER_EMAIL_DOMAIN}$` }});

    const users = buildUsers();
    await User.insertMany(users);

    const bookings = buildBookings(trips, users);

    await Booking.insertMany(bookings);

    console.log(`Seeded ${users.length} users`);
    console.log(`Seeded ${bookings.length} bookings accross ${trips.length} trips`);
};

// Close MongoDB connection and Exit
seedBookings()
    .then(async () => {
        await Mongoose.connection.close();
        process.exit(0);
    })
    .catch(async (err) => {
        console.error('Seed failed:', err.message);
        await Mongoose.connection.close();
        process.exit(1);
    });