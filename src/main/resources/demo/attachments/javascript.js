var APP = APP || {};
APP.Views.People = Backbone.View.extend({
    template: APP.getTemplate('people-list-template'),
    initialize: function() {
        this.render();
        var peopleControls = new APP.Views.PeopleControls({
            el: '#controls'
        });
        var peopleInfo = new APP.Views.PeopleInfo({
            el: '#summary'
        });
        this.showPeopleCount();
        this.listenTo(this.collection, 'reset', this.renderPeopleList);
        this.listenTo(this.collection, 'all', this.showPeopleCount);
    },
    events: {
        'click .toJSON': 'convertToJSON',
        'click .deleteAll': 'deleteAll',
        'click .addPerson': 'addPerson'
    },
    convertToJSON: function() {
        this.$('#JSON-output').html(JSON.stringify(this.collection.toJSON()));
    },
    renderPerson: function(person) {
        var personView = new APP.Views.Person({
            model: person,
            //el: '#peopleList' затирает el при каждом вызове
        });
        this.$el.find('#peopleList').append(personView.render().el);
    },
    deleteAll: function() {
        if (confirm('Delete all data?')) {
            this.collection.reset();
        }
    },
    addPerson: function() {
        var personModel = new APP.Models.Person({
            'name': this.$el.find('#name').val().trim(),
            'age': +this.$el.find('#age').val().trim(),
            'profession': this.$el.find('#profession > option:selected').text(),
        }, {
            validate: true
        });
        if (!personModel.validationError) {
            this.collection.add(personModel);
            this.renderPerson(personModel);
        } else {
            alert(personModel.validationError);
        }
    },
    renderPeopleList: function() {
        this.$el.find('#peopleList').html('');
        this.collection.each(this.renderCollection, this);
    },
    showPeopleCount: function() {
        this.$el.find('#peopleCount').html(this.collection.length);
    },
    render: function() {
        this.$el.html(this.template);
        this.collection.each(this.renderPerson, this);
    }
});
