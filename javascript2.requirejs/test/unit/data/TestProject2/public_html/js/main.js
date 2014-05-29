requirejs.config({
    baseUrl: 'js/lib',
    paths: {
        app: '../app',
        piknic: '../picnic'
    }
});
 
requirejs(['moduleLib1', 
        'js/lib/moduleLib2.js', 
        'app/moduleApp1', 
        'piknic'], 
    function(lib1, lib2, test, pik) {
    console.log(lib1.const1);
    console.log(lib2.const4);
    console.log(test.second);
    console.log(pik.date1);
});