var fs = require('fs');
var mealsData = JSON.parse(fs.readFileSync(__dirname + '/../../data/meals.json', 'utf8'));

/* GET Travel View */
const meals = (req, res) => {
    res.render('meals', { title: 'Travlr Getaways', meals: mealsData});
};

module.exports = {
    meals
};