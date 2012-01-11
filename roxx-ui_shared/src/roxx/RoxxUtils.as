package roxx
{
	import mx.collections.IList;

	public class RoxxUtils
	{
		public function RoxxUtils()
		{
			throw new Error("This is only a util class with static methods - do not instantiate");
		}

		public static function getRouteGradeDisplayString(route:*):String{
			if(!route || !route.grade){
				return "";
			}
			var ret:String = route.grade.value;
			ret += RoxxUtils.gradeQualifierToDisplayString(route.grade.qualifier);
			return ret;
		}

		public static function gradeToDisplayString(grade:*): String{
			var ret:String = grade.value + gradeQualifierToDisplayString(grade.qualifier);
			return ret;
		}

		public static function gradeQualifierToDisplayString(qualifier:*): String{
			if(qualifier != null && qualifier.id != null){
				const id: Number = qualifier.id;
				if (id == 0){
					return "+";
				} else if (id == 1){
					return "-";
				} else {
					return "";
				}
			} else {
				return "";
			}
		}
		
		public static function gradeQualifierDisplayStringToId(str: String): Number{
			if (str == "+"){
				return 0;
			} else if (str == "-"){
				return 1;
			} else {
				return Number.NaN;
			}
		}
		
		// MARKER: that is needed because there is no way to overwirte equals as it could be done in java. For that reason one
		// will get -1 for a getItemIndex that takes an Object which has a different reference but same content.
		// Note: a more generic approach is to use ObjectUtil.compare but that comaprares all properties what is not necessary for
		// our scenario.
		public static function findRouteInListById(list:IList, id:String):Object{
			for each (var e:Object in list.toArray()){
				if(e.id == id){
					return e;
				}
			}
			return null;
		}
		
		public static function findIndexOfRouteInListById(list:IList, id:String):int{
			for each (var e:Object in list.toArray()){
				if(e.id == id){
					return list.getItemIndex(e);
				}
			}
			return -1;
		}
		
	}
}