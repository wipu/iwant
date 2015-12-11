package com.example.mixedscala.editversionfirst {

    class ScalaThatDependsOnJava {

        def stringFromScala(): String = {
            var j = new com.example.mixedscala.editversionfirst.JavaHello();
            return "scala calling " + j.stringFromJava();
        }

    }

}
