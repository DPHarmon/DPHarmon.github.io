/**
 *  Dashboard Response Models.
 * 
 * Each Interface mirrors the shape returned by one endpoint under
 * /api/dashboard. These are contracts - produced by the $project stage at the
 * end of an aggregation pipeline.
 * 
 * Property names match the controller's projection.
 * 
 * Date: 8/1/2026
 * Author: Dylan Harmon
 * 
 */

/**
 *  GET /api/dashboard/revenue-by-trip
 * 
 * One entry per trip, sorted by totalRevenue descending.
 */
export interface RevenueByTrip {
    tripCode: string;
    tripName: string;
    totalRevenue: number;
    bookingCount: number;
    totalTravelers: number;
    avgBookingValue: number;
}

/**
 *  GET /api/dashboard/monthly-revenue
 * 
 *  One Entry per month, oldest first.
 */
export interface MonthlyRevenue {
    month: string;
    revenue: number;
    bookingCount: number;
    travelers: number;
}

/**
 *  GET /api/dashboard/top-trips
 * 
 * The five most frequently booked trips.
 */
export interface TopTrip {
    tripCode: string;
    tripName: string;
    perPerson: string;
    bookingCount: number;
    totalTravelers: number;
    totalRevenue: number;
}

/**
 *  GET /api/dashboard/cancellation-rate
 * 
 * A single summary of the cancellation rate and relevent data.
 */
export interface CancellationSummary {
    totalBookings: number;
    cancelled: number;
    confirmed: number;
    completed: number;
    lostRevenue: number;
    cancellationRate: number;
}