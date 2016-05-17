package kplyr.examples

import kplyr.*


fun main(args: Array<String>) {

    // Create data-frame in memory
    var df: DataFrame = SimpleDataFrame(
            StringCol("first_name", listOf("Max", "Franz", "Horst")),
            StringCol("last_name", listOf("Doe", "Smith", "Keanes")),
            IntCol("age", listOf(23, 23, 12)),
            IntCol("weight", listOf(55, 88, 82))
    )

    // Or from csv
    // val otherDF = fromCSV("path/to/file")

    // Print rows
    df                              // with default printing options
    df.print(colNames = false)      // with custom  printing options

    // Print structure
    df.glimpse()


    // Add columns with mutate
    // by adding constant values as new column
    df.mutate("salary_category", { 3 })

    // by doing basic column arithmetics
    df.mutate("age_3y_later", { it["age"] + 3 })

    // Note: kplyr dataframes are immutable so we need to (re)assign results to preserve changes.
    df = df.mutate("full_name", { it["first_name"] + " " + it["last_name"] })

    // Also feel free to mix types here since kplyr overloads to arithmetic operators like + for dataframe-columns
    df.mutate("user_id", { "id" + rowNumber() + it["last_name"] })

    // Create new attributes with string operations like matching, splitting or extraction.
    df.mutate("with_anz", { it["first_name"].asStrings().map { it!!.contains("anz") } })

    // Note: kplyr is using 'null' as missing value, and provides convenience methods to process non-NA bits
    df.mutate("first_name_restored", { it["full_name"].asStrings().rmNA { split(" ".toRegex(), 2)[1] } })


    // Resort with arrange
    df.arrange("age")
    // and add secondary sorting attributes as varargs
    df.arrange("age", "weight")


    // Subset columns with select
    df.select("last_name", "weight")    // positive selection
    df.select(-"weight", -"age")  // negative selection
    df.select({ endsWith("name") })    // selector mini-language


    // Subset rows with filter
    df.filter { it["age"] eq 23 }
    df.filter { it["weight"] gt 50 }
    df.filter({ it["last_name"].asStrings().map { it!!.startsWith("Do") }.toBooleanArray() })


    // Summarize
    // ... single summary statistic
    df.summarize("mean_age" to { it["age"].mean(true) })
    // ... multiple summary statistics
    df.summarize(
            "min_age" to { it["age"].max() },
            "max_age" to { it["age"].max() }
    )


    // Grouped operations
    val groupedDf: DataFrame = df.groupBy("age") // or provide multiple grouping attributes with varargs
    val sumDF = groupedDf.summarize("mean_weight", { it["weight"].mean(remNA = true) })

    // Optionally ungroup the data
    sumDF.ungroup()

    // generate object bindings for kotlin.
    // Unfortunately the syntax is a bit odd since we can not access the variable name by reflection
    sumDF.toKotlin("sumDF")
    // This will auto-generate and print the following data to stdout:
    data class SumDf(val first_name: String, val last_name: String, val age: Int, val weight: Int)

    val sumDfEntries = df.rows.map {
        row ->
        SumDf(row["first_name"] as String, row["last_name"] as String, row["age"] as Int, row["weight"] as Int)
    }
    // Now we can use the kplyr result table in a strongly typed way
    sumDfEntries.first().first_name
}
