var express = require('express');
var router = express.Router();
var controller = require('../controllers/news');

/*GET News page */
router.get('/', controller.news);

module.exports = router;