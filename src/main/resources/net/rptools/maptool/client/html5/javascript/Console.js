{
    "use strict";
    let REPLACEMENTS = [
	[/&/g, '&#38;'],
	[/\//g, '&#47;'],
	[/</g, '&#60;'],
	[/>/g, '&#62;'],
	[/=/g, '&#61;'],
    ];
    let seen = [];
    let serializeToString = function(arg, topLevel) {
	if (typeof arg == "object") {
	    if (seen.indexOf(arg) > -1) {
		return "...";
	    }
	    seen.push(arg);
	}
	
	if (arg instanceof Array) {
	    let toLog = [];
	    for (let obj of arg) {
		toLog.push(serializeToString(obj));
	    }
	    seen.pop();
	    return '[ ' + toLog.join(', ') + ' ]';
	}
	if (arg instanceof Object) {
	    if (arg.__proto__ !== Object.__proto__.__proto__) {
		return arg.toString();
	    }
	    let toLog = []
	    for (let key in arg) {
		let tl = ""+serializeToString(key)+": "+serializeToString(arg[key]);
		toLog.push(tl);
	    }
	    seen.pop();
	    return "{ " + toLog.join(", ") + " }";
	}
	if (typeof arg == "string" && topLevel) {
	    return arg;
	}
	try {
	    return JSON.stringify(arg);
	}
	catch {
	    return ""+arg;
	}
    };
    console.log = function(...args) {
	seen.length = 0;
	let toLog = [];
	for (let arg of args) {
	    toLog.push(serializeToString(arg, true));
	}
	let text = toLog.join(" ");
	for (let r of REPLACEMENTS) {
	    text = text.replace(r[0], r[1]);
	}
	MapTool.log('<pre>' + text + '</pre>');
	
    };
}
