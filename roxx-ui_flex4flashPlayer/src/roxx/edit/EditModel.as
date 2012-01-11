package roxx.edit
{
	import flash.display.Bitmap;
	import flash.net.FileReference;

	[Bindable]
	public class EditModel
	{

		//MARKER-05XXX: Security - FileRefernce - very special: file-acces only with preceeded user-interaction
		public var fileReference:FileReference;
		public var routeImage:Bitmap = null;

		public function EditModel()
		{
		}
	}
}