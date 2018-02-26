(function () {

    function Editor(element) {
        this.el = document.getElementById(element);
        this.init();
    }

    Editor.prototype = {
        init: function () {
            this.buttons = [];
        },
        addButton: function (btn) {
            var button = document.querySelector(btn);
            this.buttons.push(button);
        },
        _addEvent: function (element, evt, callback) {
            element.addEventListener(evt, function (e) {
                e.preventDefault();
                callback(element);
            }, false);
        },
        addAction: function (evtype, action) {
            var self = this;
            for (var i = 0; i < self.buttons.length; ++i) {
                var but = self.buttons[i];
                self._addEvent(but, evtype, action);
            }
        }
    };

    document.addEventListener("DOMContentLoaded", function () {
        var myEditor = new Editor("editor");
        myEditor.addButton("#add-abstract");
        myEditor.addButton("#add-paragraph");
        myEditor.addButton("#add-blockquote");
        myEditor.addButton("#add-h2-title");
        myEditor.addButton("#add-h3-title");
        myEditor.addButton("#add-hr");

        myEditor.addAction("click", function (bt) {
            var id = bt.getAttribute("id");
            var html = "";
            var textarea = document.querySelector("#content-text");
            var value = textarea.value;
            var startPos = textarea.selectionStart;
            var endPos = textarea.selectionEnd;
            var selectedText = value.substring(startPos, endPos);

            switch (id) {
                case "add-abstract":
                    if (selectedText == '') {
                        html = "<p class=\"am-article-lead\">\n" +
                            "    此处添加摘要\n" +
                            "</p>"
                    } else {
                        html = "<p class=\"am-article-lead\">\n" +
                            "    " + selectedText + "\n" +
                            "</p>"
                    }
                    break;
                case "add-paragraph":
                    if (selectedText == '') {
                        html = "<p>\n" +
                            "    此处添加段落\n" +
                            "</p>"
                    } else {
                        html = "<p>\n" +
                            "    " + selectedText + "\n" +
                            "</p>"
                    }
                    break;
                case "add-blockquote":
                    if (selectedText == '') {
                        html = "<blockquote>\n" +
                            "    此处添加引用\n" +
                            "</blockquote>"
                    } else {
                        html = "<blockquote>\n" +
                            "    " + selectedText + "\n" +
                            "</blockquote>"
                    }
                    break;
                case "add-h2-title":
                    if (selectedText == '') {
                        html = "<h2>\n" +
                            "    此处添加二级标题\n" +
                            "</h2>"
                    } else {
                        html = "<h2>\n" +
                            "    " + selectedText + "\n" +
                            "</h2>"
                    }
                    break;
                case "add-h3-title":
                    if (selectedText == '') {
                        html = "<h3>\n" +
                            "    此处添加三级标题\n" +
                            "</h3>"
                    } else {
                        html = "<h3>\n" +
                            "    " + selectedText + "\n" +
                            "</h3>"
                    }
                    break;
                case "add-hr":
                    html = '<hr class="am-article-divider"/>'
                    break;
                default:
                    break;
            }
            if (startPos != 0 && value.substring(endPos - 1, endPos) != '\n') {
                html = "\n" + html
            }
            if (endPos != textarea.length - 1) {
                html = html + "\n"
            }
            var before = value.substring(0, startPos)
            var after = value.substring(endPos)
            textarea.value = before + html + after
            show()
        });

    });


})();
