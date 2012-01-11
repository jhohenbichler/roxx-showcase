package roxx
{
	import mx.validators.ValidationResult;
	import mx.validators.Validator;

	public class GradeValidator extends Validator
	{

		private var results:Array;

		public function GradeValidator()
		{
			super();
		}


		public function get isValid():Boolean{
			return results != null && results.length == 0
		}


		override protected function doValidation(value:Object):Array {

			results = [];

			const input: String = value as String;

			const regex:RegExp = /([0-9]{1,2})([+-]?)(.*)/;
			const regexResult:Array = regex.exec(input);

			if(regexResult == null || regexResult.length != 4 || regexResult[3] != ""){
				results.push(new ValidationResult(true, null, "invalid grade", "a grade is a number between 1 and 13 that could be followd by a qualifier (+ or -)"));
			} else {

				const gradeNumber:Number = new Number(regexResult[1]);
				const gradeQualifier:String = regexResult[2];

				if(isNaN(gradeNumber) || !(gradeNumber >= 1 && gradeNumber <= 13)){
					results.push(new ValidationResult(true, null, "invalid grade number", "grade number must be a number between 1 and 13"));
				}

				if ( !(gradeQualifier == "" || gradeQualifier == "-" || gradeQualifier == "+")){
					results.push(new ValidationResult(true, null, "illegal grade qualifier", "grade qualifier must be empty or + or -"));
				}

			}

			return results;
		}
	}
}