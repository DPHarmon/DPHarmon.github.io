var fs = require('fs');
var roomsData = JSON.parse(fs.readFileSync(__dirname + '/../../data/rooms.json', 'utf8'));

/* GET Travel View */
const rooms = (req, res) => {
    res.render('rooms', { title: 'Travlr Getaways', rooms: roomsData});
};

module.exports = {
    rooms
};