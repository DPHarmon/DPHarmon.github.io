const mongoose = require('mongoose');

/**
 *  Booking Schema
 * 
 *  Each booking is a reference style document. Rather than embedding
 *  the full trip and user documents inside every booking, only the ObjectIds
 *  are stored. This keeps booking documents small and prevents duplicated
 *  trip data. This document style means that any query needing trip or
 *  user data must perform a $lookup at aggregation.
 *  
 *  Date: 7/30/2026
 *  Author: Dylan P Harmon
 */
const bookingSchema = new mongoose.Schema({
    trip: {
        type:mongoose.Schema.Types.ObjectId,
        ref: 'trips',
        required: true
    } ,
    user : {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'users',
        required: true
    } ,
    bookingDate: {
        type: Date,
        required: true,
        default: Date.now
    } ,
    numTravelers: {
        type: Number,
        required: true,
        min: 1
    } ,
    totalPrice: {
        type: Number,
        required: true,
        min: 0
    } ,
    status: {
        type: String,
        required: true,
        enum: ['confirmed', 'cancelled', 'completed'],
        default: 'confirmed'
    }
});

/**
 *  Indexes
 * 
 *  Compind index on trip + bookingDate. This single index serves both
 *  "revenue grouped by trip", pipeline and any query filtering by trip
 *  within a date range. The descending bookingDate matches the most common
 *  read pattern of newest bookings first (top of list).
 */
bookingSchema.index({ trip: 1, bookingDate: -1});

/**
 *  The monthly revenue trend pipeline groups on bookingDate with no 
 *  trip filter, so it needs its own index.
 */
bookingSchema.index({ bookingDate: 1});

/**
 *  Supports the cancellation rate pipeline, which groups on status. Low
 *  cardinality (three possible values), but the index still allows the
 *  grouping stage to be served without a full collection scan.
 */
bookingSchema.index({ status: 1});

const Booking = mongoose.model('bookings', bookingSchema);
module.exports = Booking;