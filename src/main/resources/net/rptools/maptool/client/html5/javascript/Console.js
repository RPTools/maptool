{
    "use strict";
    let serializeToString = function(arg) {
	if (arg instanceof Array) {
	    let toLog = [];
	    for (let obj of arg) {
		toLog.push(serializeToString(obj));
	    }
	    return '[ ' + toLog.join(', ') + ' ]';
	}
	if (arg instanceof Object) {
	    if (arg.__proto__ !== Object.__proto__.__proto__) {
		return arg.toString();
	    }
	    let toLog = []
	    for (let key in arg) {
		let tl = ""+serializeToString(key)+": "+serializeToString(arg[key]);
		toLog.push(tl)
	    }
	    return "{ " + toLog.join(", ") + " }";
	}
	try {
	    return JSON.stringify(arg);
	}
	catch {
	    return ""+arg;
	}
    };
    console.log = function(...args) {
	let toLog = [];
	for (let arg of args) {
	    toLog.push(serializeToString(arg));
	}
	MapTool.log(toLog.join(" "));
	
    };
}
