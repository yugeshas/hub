const ANSI = {
    OFF: '\u001b[0m',
    BOLD: '\u001b[1m',
    BLINK: '\u001b[5m',
    BLACK: '\u001b[30m',
    RED: '\u001b[31m',
    GREEN: '\u001b[32m',
    YELLOW: '\u001b[33m',
    BLUE: '\u001b[34m',
    MAGENTA: '\u001b[35m',
    CYAN: '\u001b[36m',
    WHITE: '\u001b[37m'
};

module.exports = {

    passed: 0,
    failed: 0,
    disabled: 0,

    failures: [],

    reportRunnerStarting: function (info) {
        console.log('\n' + ANSI.BOLD + ANSI.MAGENTA + 'Executing tests...' + ANSI.OFF);
    },

    reportSpecStarting: function (info) {
        if (info.suite.description && info.suite.description.indexOf('Frisby') != -1) {
            console.log('\n' + ANSI.BOLD + '> ' + ANSI.BLUE + info.suite.description + ANSI.OFF);
            console.log(info.description
                .replace('\t', '')
                .replace('\n', ''));
        } else {
            console.log('\n' + ANSI.BOLD + '> ' + ANSI.BLUE + info.description + ANSI.OFF);
        }
    },

    reportSpecResults: function (info) {
        var results = info.results();
        var status;
        if (results.skipped) {
            status = 'DISABLED';
        } else if (results.passed()) {
            status = 'PASSED';
        } else {
            status = 'FAILED';
        }

        switch (status) {

            case 'PASSED':
                console.log(ANSI.BOLD + '< ' + ANSI.GREEN + status + ANSI.OFF);
                this.passed++;
                break;

            case 'FAILED':
                console.log(ANSI.BOLD + '< ' + ANSI.RED + status + ANSI.OFF);
                this.failed++;
                var self = this;
                results.items_.forEach(function (item) {
                    if (!item.passed_) {
                        console.log(ANSI.RED + item.trace.stack + ANSI.OFF);
                        self.failures.push(item.trace.stack);
                    }
                });
                break;

            case 'DISABLED':
                this.disabled++;
                console.log(ANSI.BOLD + '< ' + ANSI.YELLOW + status + ANSI.OFF);
                break;

            default:
                console.log(ANSI.BOLD + '< ' + ANSI.CYAN + status + ANSI.OFF);
        }
    },

    reportSuiteResults: function (info) {
        // don't output anything
    },

    reportRunnerResults: function (info) {
        if (this.failed) {
            var twentyDashes = new Array(20).join('-');
            console.log('\n' + twentyDashes + ' FAILURE SUMMARY ' + twentyDashes);
            this.failures.forEach(function (failure) {
                console.log('\n' + ANSI.RED + failure + ANSI.OFF);
            });
        }

        console.log('\n' + new Array(50).join('-') + '\n');
        console.log(ANSI.GREEN + 'PASSED: ' + ANSI.BOLD + this.passed + ANSI.OFF);
        console.log(ANSI.RED + 'FAILED: ' + ANSI.BOLD + this.failed + ANSI.OFF);
        console.log(ANSI.YELLOW + 'DISABLED: ' + ANSI.BOLD + this.disabled + ANSI.OFF + '\n');

        process.exit(this.failed ? 1 : 0);
    }

};
