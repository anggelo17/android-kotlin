package edu.uga.eits.android.model

import edu.uga.eits.android.extensions.EnumCompanion
import org.jetbrains.anko.withAlpha

object Constants{

    val baseURL = "https://my.uga.edu"
    val staticPath = "/test/staticjsons"

    val staticBaseURL = "$baseURL$staticPath"
    val UGA_RED = 0xff0000.withAlpha(0xCC)
    val NEAR_BY_FOR_ETA :Meters = 350.0f
    val WALKABLE_DISTANCE:Meters = 500.0f
    val WALKABLE_MATRIX_CUT_OFF:Meters = WALKABLE_DISTANCE * 10f
    val NEAR_BY_CUT_OFF : Meters = WALKABLE_DISTANCE * 20f
    val SORT_METRICS_STRAIGHT_WALKABLE_CUT_OFF = WALKABLE_MATRIX_CUT_OFF + WALKABLE_DISTANCE
    val MAX_DISTANCE:Meters = Meters.MAX_VALUE
    val displayOnParkingFeatureTypes = listOf("Bike Rack","Vending Machine")
     val KEY_ALIAS = "sitepoint"
     val KEYSTORE = "AndroidKeyStore"
     val PREFERENCES_KEY_EMAIL = "email"
     val PREFERENCES_KEY_PASS = "pass"
     val PREFERENCES_KEY_IV = "iv"
    val CURRENTTABBUS = "CurrentTabBus"
    val CURRENTTABPARKING = "CurrentTabParking"
}
enum class CategoryType{
    HeaderItem,None,ALL,GrayHeaderItem,
    DetailViewItem,
    UGA_Bus_Stop,Athens_Transit_Stop,
    UGA_Routes,Athens_Routes,
    BlankItem,WebLink,PhoneNumber,

    Parking,Parking_Rates,
    DetailsViewItem,

    Building,Dining,Computer_Lab,Print_Kiosk,Visitor_Parking,Bike_Rack,Bike_Repair_Station,Electric_Charging_Station,

    Athletics,Green_Space,Library,Parking_Deck,Parking_Lot,Residence_Hall,Vending_Machine,UPS_Drop_Box,

    My_Places,

    Health_Center,Mental_Wellness,Sexual_Assault,General_Resources //Wellness Resources
    ;
    companion object : EnumCompanion<CategoryType>(values(),None) {
        val  displayOnPlaceCategoryTypes = listOf(
                My_Places,
                Building, Dining,
                Athletics, Bike_Rack, Bike_Repair_Station, Computer_Lab, Electric_Charging_Station,
                Green_Space, Library, Parking_Deck, Print_Kiosk, Residence_Hall, Vending_Machine,
                Visitor_Parking, UPS_Drop_Box)
        val displayOnPlaceDetailTypes = listOf(UGA_Bus_Stop,Parking, Dining,Computer_Lab,Print_Kiosk,Visitor_Parking,Bike_Rack,Bike_Repair_Station)
        val displayOnParkingDetailTypes = listOf(Parking_Rates,UGA_Bus_Stop,Bike_Rack)
        val displayOnWellnessResources = listOf(Health_Center,Mental_Wellness,Sexual_Assault,General_Resources)
    }
    fun toTitle() = when(this){
        Building,Bike_Rack,Bike_Repair_Station,Computer_Lab,Green_Space,Parking_Deck,Print_Kiosk,Residence_Hall,Vending_Machine -> this.toString().replace("_"," ")+"s";
        Athletics -> "Athletics Locations"; Library -> "Libraries";UPS_Drop_Box -> "UPS Drop Boxes"
        else -> this.toString().replace("_"," ")
    }
    fun toDescription() = when(this){
        Sexual_Assault -> "List of Resources Concerning Sexual Assault";General_Resources -> "List of Wellness Resources"
        else -> ""
    }
    fun isType(str:String) = (this.toString() == str)
}
