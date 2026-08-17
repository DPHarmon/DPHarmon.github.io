/**
 *  Dashboard Controller.
 * 
 *  Sets up the 4 pipelines used for the dashboard display.
 * 
 * Date: 7/31/2026
 * Author: Dylan Harmon
 */

const Booking = require('../models/bookings');

/**
 *  Date is stored in MongoDB as UTC. Set Reporting timezone.
 */
const REPORT_TIMEZONE = 'America/Chicago';

/**
 * Number of months included in the trend endpoint, and number of trips
 *  returned by the top trips endpoint.
 */
const TREND_MONTHS = 12;
const TOP_TRIPS_LIMIT = 5;

/**
 *  Cancelled bookings are excluded from revenue and traveler counters.
 *  Declared exclude.
 */
const EXCLUDE_CANCELLED = { status: {$ne: 'cancelled' } };

/**
 *  GET /api/dashboard/revenue-by-trip
 * 
 *  Total revenue and booking volume per trip.
 * 
 *  $lookup executes once per trip rather than per booking.
 */
const revenueByTrip = async (req, res) => {
    try {
        const results = await Booking.aggregate([
            // Exclude Cancelled Bookings
            { $match: EXCLUDE_CANCELLED },

            /**
             *  Collapse bookings into one document per trip. 
             *  _id -> grouping key.
             */
            {
                $group: {
                    _id: '$trip',
                    totalRevenue: { $sum: '$totalPrice' },
                    bookingCount: { $sum: 1 },
                    totalTravelers: { $sum: '$numTravelers' }
                }
            },

            // Left Outer Join
            {
                $lookup: {
                    from: 'trips',
                    localField: '_id',
                    foreignField: '_id',
                    as: 'tripDetails'
                }
            },

            /**
             *  $unwind flattens the single-element array so the fields
             *  can be addressed directly.
             */
            { $unwind: '$tripDetails' },

            /**
             *  Structure for charting layer. 
             */
            {
                $project: {
                    _id: 0,
                    tripCode: '$tripDetails.code',
                    tripName: '$tripDetails.name',
                    totalRevenue: { $round: ['$totalRevenue', 2] },
                    bookingCount: 1,
                    totalTravelers: 1,
                    
                    // Derive Avg Booking Value
                    avgBookingValue: {
                        $round: [
                            { $divide: ['$totalRevenue', '$bookingCount'] },
                            2
                        ]
                    }
                }
            },

            { $sort: { totalRevenue: -1 } }
        ]);

        return res.status(200).json(results);
    } catch (err) {

        return res.status(500).json({ message: err.message });
    }
};

/**
 *  GET api/dashboard/monthly-revenue
 * 
 *  Revenue and booking volume per month across the trailing window.
 * 
 *  The window is bounded in the $match stage rather than filtered after
 *  grouping. The months outside the range never enter the pipeline.
 */
const monthlyRevenue = async (req, res) => {
    try {
        /**
         *  Compute the cutoff.
         */
        const cutoff = new Date();
        cutoff.setMonth(cutoff.getMonth() - (TREND_MONTHS - 1));
        cutoff.setDate(1);
        cutoff.setHours(0, 0, 0, 0);

        const results = await Booking.aggregate([
            {
                $match: {
                    ...EXCLUDE_CANCELLED,
                    bookingDate: { $gte: cutoff }
                }
            },

            /**
             *  Grouped on formatted year-month string.
             */
            {
                $group: {
                    _id: {
                        $dateToString: {
                            format: '%Y-%m', 
                            date: '$bookingDate',
                            timezone: REPORT_TIMEZONE
                        }
                    },
                    
                    revenue: { $sum: '$totalPrice' },
                    bookingCount: { $sum: 1 },
                    travelers: { $sum: '$numTravelers' }
                }
            },

            {
                $project: {
                    _id: 0,
                    month: '$_id',
                    revenue: { $round: ['$revenue', 2] },
                    bookingCount: 1,
                    travelers: 1
                }
            },

            // Chronological Order, oldest first
            { $sort: { month: 1 } }
        ]);

        return res.status(200).json(results);
    } catch (err) {
        return res.status(500).json({ message: err.message });
    }
};

/**
 *  GET api/dashboard/top-trips
 * 
 *  THE TOP 5!
 * 
 *  Ranks on booking count (popularity) rather than revenue. 
 */
const topTrips = async (req, res) => {
    try {
        const results = await Booking.aggregate([
            {$match: EXCLUDE_CANCELLED },

            {
                $group: {
                    _id: '$trip',
                    bookingCount: { $sum: 1 },
                    totalTravelers: { $sum: '$numTravelers' },
                    totalRevenue: { $sum: '$totalPrice' }
                }
            },

            /**
             *  SORT and LIMIT befor the join. 
             */
            { $sort: { bookingCount: -1 } },
            { $limit: TOP_TRIPS_LIMIT  },

            {
                $lookup: {
                    from: 'trips',
                    localField: '_id',
                    foreignField: '_id',
                    as: 'tripDetails'
                }
            },
            { $unwind: '$tripDetails' },

            {
                $project: {
                    _id: 0,
                    tripCode: '$tripDetails.code',
                    tripName: '$tripDetails.name',
                    perPerson: '$tripDetails.perPerson',
                    bookingCount: 1,
                    totalTravelers: 1,
                    totalRevenue: { $round: ['$totalRevenue', 2] }
                }
            }
        ]);

        return res.status(200).json(results);
    } catch (err) {
        return res.status(500).json({ message: err.message });
    }
};

/**
 *  GET api/dashboard/cancellation-rate
 * 
 *  Booking counts by status and the overall cancellation rate.
 */
const cancellationRate = async (req, res) => {
    try {
        const results = await Booking.aggregate([
            /**
             *  Grouping on null collapses the entire `collection` into a single
             *  document. Count one status by adding 1 when $cond matches. Count
             *  all in a single pass.
             */
            {
                $group: {
                    _id: null,
                    totalBookings: { $sum: 1 },
                    cancelled: {
                        $sum: {
                            $cond: [{ $eq: [ '$status', 'cancelled'] }, 1, 0]
                        }
                    },
                    confirmed: {
                        $sum: {
                            $cond: [{ $eq: ['$status', 'confirmed'] }, 1, 0]
                        }
                    },
                    completed: {
                        $sum: {
                            $cond: [{ $eq: ['$status', 'completed'] }, 1, 0]
                        }
                    },

                    /**
                     *  Revenue lost to cancellations.
                     */
                    lostRevenue: {
                        $sum: {
                            $cond: [
                                { $eq: ['$status', 'cancelled'] },
                                '$totalPrice',
                                0
                            ]
                        }
                    }
                }
            },
            
            {
                $project: {
                    _id: 0,
                    totalBookings: 1,
                    cancelled: 1,
                    confirmed: 1,
                    completed: 1,
                    lostRevenue: { $round: ['$lostRevenue', 2] },

                    /**
                     *  $cond guards the division against an empty collection.
                     *  Fails if divides by zero rather than reporting a rate
                     *  of zero.
                     */
                    cancellationRate: {
                        $cond: [
                            { $eq: ['$totalBookings', 0] }, 
                            0,
                            {
                                $round: [
                                    {
                                        $multiply: [
                                            {
                                                $divide: [
                                                    '$cancelled',
                                                    '$totalBookings'
                                                ]
                                            },
                                            100
                                        ],
                                    },
                                    2
                                ]
                            }
                        ]
                    }
                }
            }
        ]);

        /**
         *  Return a zeroed object in the case a collection is empty.
         *  This keeps the response shape stable so the client never
         *  has to branch on an undefined result.
         */
        const summary = results[0] || {
            totalBookings: 0,
            cancelled: 0,
            confirmed: 0,
            completed: 0,
            lostRevenue: 0,
            cancellationRate: 0
        };

        return res.status(200).json(summary);
    } catch (err) {
        return res.status(500).json({ message: err.message });
    }

};

// EXPORT
module.exports = {
    revenueByTrip,
    monthlyRevenue,
    topTrips,
    cancellationRate
};