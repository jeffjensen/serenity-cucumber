package net.serenitybdd.cucumber.outcomes

import com.github.goldin.spock.extensions.tempdir.TempDir
import net.thucydides.core.reports.OutcomeFormat
import net.thucydides.core.reports.TestOutcomeLoader
import net.serenitybdd.cucumber.integration.BasicArithemticWithTablesAndBackgroundScenario
import net.serenitybdd.cucumber.integration.BasicArithemticWithTablesScenario
import net.serenitybdd.cucumber.integration.SimpleTableScenario
import net.serenitybdd.cucumber.integration.SimpleTableScenarioWithFailures
import spock.lang.Specification

import static net.thucydides.core.model.TestResult.FAILURE
import static net.thucydides.core.model.TestResult.SUCCESS
import static net.serenitybdd.cucumber.util.CucumberRunner.serenityRunnerForCucumberTestRunner

/**
 * Created by john on 23/07/2014.
 */
class WhenCreatingSerenityTestOutcomesForTableDrivenScenarios extends Specification {

    @TempDir
    File outputDirectory

    /*
          Scenario Outline: Buying lots of widgets
            Given I want to purchase <amount> widgets
            And a widget costs $<cost>
            When I buy the widgets
            Then I should be billed $<total>
          Examples:
          | amount | cost | total |
          | 0      | 10   | 0     |
          | 1      | 10   | 10    |
          | 2      | 10   | 20    |
          | 2      | 0    | 0     |
     */
    def "should run table-driven scenarios successfully"() {
        given:
        def runtime = serenityRunnerForCucumberTestRunner(SimpleTableScenario.class, outputDirectory);

        when:
        runtime.run();
        def recordedTestOutcomes = new TestOutcomeLoader().forFormat(OutcomeFormat.JSON).loadFrom(outputDirectory);

        then: "there should a test outcome for each scenario"
        recordedTestOutcomes.size() == 2

        and:
        def testOutcome = recordedTestOutcomes[0]
        testOutcome.title == "Buying lots of widgets"

        and: "there should be one step for each row in the table"
        testOutcome.stepCount == 7

        and: "each of these steps should contain the scenario steps as children"
        def childSteps1 = testOutcome.testSteps[0].children.collect { step -> step.description }
        def childSteps2 = testOutcome.testSteps[1].children.collect { step -> step.description }
        def childSteps3 = testOutcome.testSteps[2].children.collect { step -> step.description }

        childSteps1 == ['Given I have $100','Given I want to purchase 0 widgets', 'And a widget costs $10', 'When I buy the widgets', 'Then I should be billed $0']
        childSteps2 == ['Given I have $100','Given I want to purchase 1 widgets', 'And a widget costs $10', 'When I buy the widgets', 'Then I should be billed $10']
        childSteps3 == ['Given I have $100','Given I want to purchase 2 widgets', 'And a widget costs $10', 'When I buy the widgets', 'Then I should be billed $20']

        and:
        recordedTestOutcomes[0].dataTable.rows.collect { it.result } == [SUCCESS, SUCCESS, SUCCESS, SUCCESS, SUCCESS, SUCCESS, SUCCESS]
        recordedTestOutcomes[1].dataTable.rows.collect { it.result } == [FAILURE, SUCCESS, SUCCESS]

        and:
        testOutcome.exampleFields == ["amount", "cost","total"]
        testOutcome.dataTable.rows[0].stringValues == ["0","10","0"]
        testOutcome.dataTable.rows[1].stringValues == ["1","10","10"]
    }

    def "should run table-driven scenarios with failing rows"() {
        given:
        def runtime = serenityRunnerForCucumberTestRunner(SimpleTableScenarioWithFailures.class, outputDirectory);

        when:
        runtime.run();
        def recordedTestOutcomes = new TestOutcomeLoader().forFormat(OutcomeFormat.JSON).loadFrom(outputDirectory);
        def testOutcome = recordedTestOutcomes[0]

        then:
        testOutcome.title == "Buying lots of widgets"

        and: "there should be one step for each row in the table"
        testOutcome.stepCount == 4

        and:
        testOutcome.dataTable.rows.collect { it.result } == [SUCCESS, SUCCESS, FAILURE, SUCCESS]

        and:
        testOutcome.errorMessage == "expected:<[5]0> but was:<[2]0>"
    }



    def "should handle multiple example tables"() {
        given:
        def runtime = serenityRunnerForCucumberTestRunner(BasicArithemticWithTablesScenario.class, outputDirectory);

        when:
        runtime.run();
        def recordedTestOutcomes = new TestOutcomeLoader().forFormat(OutcomeFormat.JSON).loadFrom(outputDirectory);
        def testOutcome = recordedTestOutcomes[0]

        then:
        testOutcome.title == "Many additions"

        and:
        testOutcome.dataTable.dataSets.size() == 2

        and:
        testOutcome.dataTable.dataSets[0].name == "Single digits"
        testOutcome.dataTable.dataSets[0].description == "With just one digit"
        testOutcome.dataTable.dataSets[0].rows.size() == 2

        and:
        testOutcome.dataTable.dataSets[1].name == "Double digits"
        testOutcome.dataTable.dataSets[1].description == "With more digits than one"
        testOutcome.dataTable.dataSets[1].rows.size() == 3

    }

    def "should handle multiple example tables with backgrounds"() {
        given:
        def runtime = serenityRunnerForCucumberTestRunner(BasicArithemticWithTablesAndBackgroundScenario.class, outputDirectory);

        when:
        runtime.run();
        def recordedTestOutcomes = new TestOutcomeLoader().forFormat(OutcomeFormat.JSON).loadFrom(outputDirectory);
        def testOutcome = recordedTestOutcomes[0]

        then:
        testOutcome.title == "Many additions"

        and:
        testOutcome.dataTable.dataSets.size() == 2

        and:
        recordedTestOutcomes.size() == 1
        testOutcome.stepCount == 5

        and:
        testOutcome.backgroundDescription == "The calculator should be set up and all that"
        and:
        testOutcome.dataTable.dataSets[0].name == "Single digits"
        testOutcome.dataTable.dataSets[0].description == "With just one digit"
        testOutcome.dataTable.dataSets[0].rows.size() == 2

        and:
        testOutcome.dataTable.dataSets[1].name == "Double digits"
        testOutcome.dataTable.dataSets[1].description == "With more digits than one"
        testOutcome.dataTable.dataSets[1].rows.size() == 3

    }



}