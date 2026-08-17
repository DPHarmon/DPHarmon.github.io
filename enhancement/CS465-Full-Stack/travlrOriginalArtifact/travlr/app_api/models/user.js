const mongoose = require('mongoose');
const crypto = require('crypto');
const jwt = require('jsonwebtoken');

const userSchema = new mongoose.Schema({
    email: {
        type: String,
        unique: true,
        required: true
    },
    name: {
        type: String,
        required: true
    },
    hash: String,
    salt: String
});

// Method to set the password for the user record
// @param - The user's new password
// Generates a cryptographically random 16-byte salt to be
// utilized to generate our hash. It stores the salt value and the 
// resulting hashed password in the user record.
userSchema.methods.setPassword = function(password) {
    this.salt = crypto.randomBytes(16).toString('hex');
    this.hash = crypto.pbkdf2Sync(password, this.salt,
        1000, 64, 'sha512').toString('hex');
};

// A Boolean method that takes a single argument -> the user's password
// this method makes use of the pbkdf2Sync method from setPassword to generate
// the password hash that is compared to the stored value.
// The processing in this method must match the processing in the previous
// for a test of equality to be valid.
userSchema.methods.validPassword = function(password) {
    var hash = crypto.pbkdf2Sync(password,
        this.salt, 1000, 64, 'sha512').toString('hex');
    return this.hash === hash;
};

// Method to generate a JSON Web Token for the current record
userSchema.methods.generateJWT = function() {
    return jwt.sign(
        { // Payload for our JSON Web Token
            _id: this._id,
            email: this.email,
            name: this.name,    
        },
        process.env.JWT_SECRET, // SECRET stored in .env file
        { expiresIn: '1h' });   // Token expires an hour from creation
};


const User = mongoose.model('users', userSchema);
module.exports = User;