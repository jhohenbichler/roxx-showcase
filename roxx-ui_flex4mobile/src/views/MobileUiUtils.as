package views
{
	import flash.display.DisplayObject;
	
	import mx.managers.PopUpManager;
	
	import roxx.CreateAndEditRoutePopUp;

	public class MobileUiUtils
	{
		public function MobileUiUtils()
		{
		}
		
		public static function openCreateAndEditPopup(displayObject:DisplayObject, route:*, createNew: Boolean):void {
			function saveOrUpdate(route: *, doNotChangeImage: Boolean):void{
				Main.instance.roxxHTTPDataSource.saveOrUpdateRouteWithoutImage(route, doNotChangeImage);
			}
			function popupSaveOrUpdateHandler(route:*, doNotChangeImage: Boolean):void {
				saveOrUpdate(route, doNotChangeImage);
			}
			var popup:CreateAndEditRoutePopUp = CreateAndEditRoutePopUp(PopUpManager.createPopUp( displayObject, CreateAndEditRoutePopUp , true));
			popup.popupSaveOrUpdateHandler = popupSaveOrUpdateHandler;
			popup.route = route;
			popup.createNew = createNew;
		}
		
		
	}
}