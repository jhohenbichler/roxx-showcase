package roxx
{
	import flash.display.Bitmap;
	import flash.display.Loader;
	import flash.events.DataEvent;
	import flash.events.IOErrorEvent;
	import flash.net.URLRequest;
	import flash.net.URLRequestMethod;
	import flash.net.URLVariables;
	import flash.system.LoaderContext;
	
	public class RoxxHTTPDataSource
	{
		import com.adobe.serialization.json.JSON;
		
		import flash.events.Event;
		import flash.events.ProgressEvent;
		import flash.net.FileReference;
		
		import mx.collections.ArrayCollection;
		import mx.events.FlexEvent;
		import mx.managers.CursorManager;
		import mx.rpc.events.FaultEvent;
		import mx.rpc.events.ResultEvent;
		import mx.rpc.http.HTTPService;
		import mx.utils.ObjectUtil;
		import mx.utils.URLUtil;
		
		public var host: String = null;
		public var port: String = null;
		
		
		public function get apiUrl():String{
			return "http://" + host + ":" + port + "/api/";
		}
		
		private var _outstandingResponses: Number = 0;
		
		/**
		 * Note: this implementation is not ready for production use.
		 * It is programmed that way: If some data is known to be missing it returns true.
		 * But for every error that happens, all outstanding work monitoring is reset. For
		 * that reason after one particular error the ruslt might be false but there are still
		 * some jobs running...
		 */
		[Bindable]
		public function get workInProgress():Boolean{
			if(_outstandingResponses <= 0)
				return false;
			return true;
		}
		
		private function set workInProgress(value:Boolean):void{
			// note: ugly work-around to get databinding work
		}
		
		private function incOutstandingResponses():void{
			_outstandingResponses++;
			workInProgress = !workInProgress;
		}
		
		private function decOutstandingResponses():void{
			if(_outstandingResponses > 0) _outstandingResponses--;
			workInProgress = !workInProgress;
		}
		
		
		[Bindable]
		public var errorText: String = null;
		
		public var loadImageHandler: Function;
		public var routesLoadedHandler: Function;
		
		private var lastSavedOrUpdatedRouteId: String;
		public var lastSavedOrUpdatedRouteIdChangedHandler: Function;
		
		
		private var imageLoader:Loader = new Loader();
		
		public function loadRoxxRoutes():void{
			incOutstandingResponses();
			//MARKER-04XXX: HTTPService, could also be defined declarative in MXML
			const httpService:HTTPService = new HTTPService();
			httpService.url = apiUrl + "routes"
			httpService.resultFormat = HTTPService.RESULT_FORMAT_TEXT;
			httpService.requestTimeout = 100;
			
			
			//MARKER-01008: Event-Driven
			function onGetRoxxRoutesResult(event:ResultEvent):void
			{
				decOutstandingResponses();
				var rawData:String = String(event.result);
				
				if(rawData == ""){
					errorText = "ERROR: json stream is just empty";
					return;
				}
				
				trace("rawData: " + rawData);
				
				// decode the data to ActionScript using the JSON API
				// in this case, the JSON data is a serialize Array of Objects.
				var arr:Array = (JSON.decode(rawData) as Array);
				
				routesLoadedHandler(arr);
			}
			
			//MARKER-01009: funktion parameter
			httpService.addEventListener(ResultEvent.RESULT, onGetRoxxRoutesResult);
			httpService.addEventListener(FaultEvent.FAULT, httpServiceErrorHandler);
			httpService.send();
		}
		
		
		
		private static function getRouteJSONData(route: *):String{
			//TODO 11.04.2011 hohenbichler: is there a better way to kill or ignore mx_internal_uid":"E69BA87F-3A9F-CFD9-A252-4455A5AAD982
			const r:* = ObjectUtil.copy(route);
			delete r.mx_internal_uid;
			const json:String = JSON.encode(r);
			trace(json);
			return json;
		}
		
		
		/**
		 * If the route has the property id defined then an update will be done.
		 * Otherwise the route will be created.
		 */
		public function saveOrUpdateRouteWithoutImage(route:*, doNotChangeImage: Boolean):void
		{
			incOutstandingResponses();
			const httpService:HTTPService = new HTTPService();
			httpService.url = apiUrl + "routes";
			// note: if we don't set the content type Flash will use some default and will also overwrite the POST with a GET... how stupid...
			httpService.contentType = "application/json";
			httpService.method = URLRequestMethod.POST;
			
			if(route.id){
				// update existing route
				
				/*
				* REST-Workaround
				*/
				
				// note: Flash could only do GET and POST for the other RSEST-Functions
				// we have to relay on the server, that it respects the header
				// X-HTTP-Method-Override. Here we Say: Server, please overwrite the POST
				// with a DELETE.
				httpService.method = URLRequestMethod.POST;
				// note: doNotChangeImage is also a workaround cause by flash-player limitations.
				// See CreateAndEditRoutePopUp.saveOrUpdate for Details
				httpService.headers = {"X-HTTP-Method-Override":"POST", "doNotChangeImage":doNotChangeImage.toString()};
				
			} else {
				// save as new - leave the method type POST as is
			}
			
			
			function onSaveRoxxRouteResult(event:ResultEvent):void
			{
				const id:String = getIdFromEventResult(event.result);
				loadRoxxRoutes();
				lastSavedOrUpdatedRouteId = id;
				if(lastSavedOrUpdatedRouteIdChangedHandler != null)
					lastSavedOrUpdatedRouteIdChangedHandler(id);
				decOutstandingResponses();
			}
			
			httpService.resultFormat = HTTPService.RESULT_FORMAT_TEXT;
			httpService.addEventListener(ResultEvent.RESULT, onSaveRoxxRouteResult);
			httpService.addEventListener(FaultEvent.FAULT, httpServiceErrorHandler);
			
			const json:String = getRouteJSONData(route);
			httpService.send(json);
		}
		
		
		public function saveOrUpdateRouteWithImage(route:*, fileReference: FileReference):void
		{
			
			incOutstandingResponses();
			
			function onProgress(evt:ProgressEvent):void
			{
				trace("Loaded " + evt.bytesLoaded + " of " + evt.bytesTotal + " bytes.");
				//				fileUploadProgress.text = "Loaded " + evt.bytesLoaded + " of " + evt.bytesTotal + " bytes.";
			}
			
			function onComplete(evt:Event):void
			{
				trace("File was successfully loaded.");
				loadRoxxRoutes();
				fileReference = null;
				decOutstandingResponses();
			}
			
			function onUploadCompleteData(evt: DataEvent):void
			{
				const id:String = getIdFromEventResult(evt.data);
				lastSavedOrUpdatedRouteId = id;
				lastSavedOrUpdatedRouteIdChangedHandler(id);
			}
			
			fileReference.addEventListener(ProgressEvent.PROGRESS, onProgress);
			fileReference.addEventListener(Event.COMPLETE, onComplete);
			fileReference.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, onUploadCompleteData);
			
			var request:URLRequest = new URLRequest(apiUrl + "routes")
			var params:URLVariables = new URLVariables();
			const json:String = getRouteJSONData(route);
			trace("json being sent to server: " + json);
			params.json = json;
			request.method = URLRequestMethod.POST;
			request.data = params;
			request.contentType = "multipart/form-data";
			try
			{
				fileReference.upload(request, "image");
			}
			catch (error:Error)
			{
				trace("Unable to upload file.");
			}
		}
		
		private function getIdFromEventResult(result:Object):String
		{
			
			const resultStr:String = result as String;
			if(resultStr == ""){
				trace("Result save or update is just empty but should contain the ID of the created or updated route.");
				return null;
			}
			
			// decode the data to ActionScript using the JSON API
			// in this case, the JSON data is a serialize Array of Objects.
			const jsonObj:Object = (JSON.decode(resultStr));
			const id:String = jsonObj.id;
			trace("id from result: " + id);
			return id;
		}
		
		
		
		public function remove(rout: *):void{
			incOutstandingResponses();
			const httpService:HTTPService = new HTTPService();
			
			function onDeleteRoxxRouteResult(event:ResultEvent):void
			{
				const id:String = getIdFromEventResult(event.result);
				trace("successfully deleted route: " + id);
				loadRoxxRoutes();
				lastSavedOrUpdatedRouteId = id;
				decOutstandingResponses();
			}
			
			httpService.url=apiUrl + "route/" + rout.id;
			httpService.resultFormat = HTTPService.RESULT_FORMAT_TEXT;
			httpService.addEventListener(ResultEvent.RESULT, onDeleteRoxxRouteResult);
			httpService.addEventListener(FaultEvent.FAULT, httpServiceErrorHandler);
			
			
			/*
			* REST-Workaround
			*/
			
			//MARKER-04XXX: REST with Flex
			
			// note: Flash could only do GET and POST for the other RSEST-Functions
			// we have to relay on the server, that it respects the header
			// X-HTTP-Method-Override. Here we Say: Server, please overwrite the POST
			// with a DELETE.
			httpService.method = URLRequestMethod.POST;
			httpService.headers = {"X-HTTP-Method-Override":"DELETE"};
			
			// set some dummy data to prevent flex from doing some stupid things that mess up our header
			httpService.request['foo'] = 'I really want a POST - at least flash should think so...';
			
			
			httpService.send();
		}
		
		
		private function httpServiceErrorHandler(event:FaultEvent):void{
			
			//something went wrong and to not show up a wrong BusyInidicator, we preventive reset it counter
			_outstandingResponses = 0;
			
			
			errorText = event.message.toString();
			trace("ERROR: " + event.statusCode + " message: " + errorText);
			trace("Check if the givene server is reachable!");
		}
		
		
		private function privateLoadImageResultHandler(event:Event):void{
			const image:Bitmap = event.currentTarget.content;
			trace("route image successfully loaded");
			loadImageHandler(image);
		}
		
		/**
		 * Remove all listeners that might listen for image loading
		 * that has been issued before the current one (fast clicking user?)
		 */
		public function cancelImageLoading():void{
			imageLoader.contentLoaderInfo.removeEventListener(Event.COMPLETE, privateLoadImageResultHandler);
		}
		
		
		public function loadRouteImage(routeId: String):void{
			
			function errorHandler(event:IOErrorEvent):void{
				const msg:String = "Image could not be loaded. It is likely that the image does not exist. Error message is: " + event.text;
				trace("Error loading image: " + msg);
			}
			
			trace("loading image for route: " + routeId);
			
			imageLoader.contentLoaderInfo.addEventListener(Event.COMPLETE, privateLoadImageResultHandler);
			imageLoader.contentLoaderInfo.addEventListener(IOErrorEvent.IO_ERROR, errorHandler);
			
			//MARKER-05XXX: Security crossdomain.xml
			
			// Note: To prevent SecurityErrors set checkPolicyFile to true.
			// Anyway the crossdomain.xml file must be placed on the server that hosts the images.
			var loaderContext:LoaderContext = new LoaderContext();
			loaderContext.checkPolicyFile = true;
			const imageURI: String = apiUrl + "route/" + routeId + "/image";
			
			imageLoader.load( new URLRequest(imageURI), loaderContext );
		}
		
	}
}