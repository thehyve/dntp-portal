var lang = 'nl';
console.log(process.argv);
if (process.argv.length > 2) {
    lang = process.argv[2];
}

var messages = require('./messages_'+lang+'.js');

for(var i in messages) {
  console.log(i + "\t" + messages[i]);
}

