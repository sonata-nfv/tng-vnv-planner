package com.github.tng.vnv.planner.data.service

import com.github.tng.vnv.planner.model.PackageMetadata

interface CatalogueService {

    def discoverAssociatedNssAndTests(PackageMetadata packageMetadata)
}