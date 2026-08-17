var fs = require('fs');
var newsData = JSON.parse(fs.readFileSync(__dirname + '/../../data/news.json', 'utf8'));

const news = (req, res) => {
    res.render('news', {
        title: 'Travlr Getaways',
        latestNews: newsData.latestNews,
        vacationTips: newsData.vacationTips,
        article: newsData.article
    });
};

module.exports = {
    news
}