package roxx
{
	import flash.events.Event;

	public class SelectedMapAreaChangedEvent extends Event
	{
		public static const SELECTED_AREA_CHANGED:String = "SELECTED_AREA_CHANGED";

		public function SelectedMapAreaChangedEvent(type:String, bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
		}
	}
}