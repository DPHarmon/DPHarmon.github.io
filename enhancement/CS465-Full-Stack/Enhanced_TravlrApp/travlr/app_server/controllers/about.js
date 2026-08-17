var fs = require('fs');
var aboutData = JSON.parse(fs.readFileSync(__dirname + '/../../data/about.json', 'utf8'));

const about = (req, res) => {
    res.render('about', {
        title: 'Travlr Getaways',
        heading: aboutData.heading,
        paragraphs: aboutData.paragraphs,
        crews: aboutData.crews,
        community: aboutData.community,
        templateDetails: aboutData.templateDetails
    });
};

module.exports = {
    about
}